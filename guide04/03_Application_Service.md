# Chapter 3: 申請受付マイクロサービス(Application Service)の実装

いよいよ最初のマイクロサービスである「申請受付サービス」を構築します。このサービスは、顧客からのローン申請を受け付け、プロセス全体のライフサイクルを管理する責務を持ちます。本章では、オニオンアーキテクチャの原則に従い、ドメイン層からインフラ層までを段階的に実装していきます。

## 3.1. ドメイン層の実装

ドメイン層は、ビジネスルールとロジックの心臓部です。外部の技術的な関心事から隔離され、純粋なビジネス知識をコードとして表現します。

### `LoanApplication` 集約 (Aggregate)

このサービスの核となるのが`LoanApplication`集約です。集約は、一貫性を保つべきオブジェクトのまとまりであり、トランザクションの境界となります。

-   **`LoanApplication.java`**:
    -   Java 17の`record`は不変なデータ構造を作るのに適していますが、状態を持つエンティティには伝統的なクラスを使用します。
    -   `submit()`メソッドは、この集約のビジネスルール（例: 申請額は正でなければならない）をカプセル化し、成功時に`LoanApplicationSubmitted`イベントを生成します。

```java
// application-service/src/main/java/com/example/loan/domain/LoanApplication.java

public class LoanApplication extends AggregateRoot<LoanApplicationId> {

    private final CustomerId customerId;
    private final Money amount;
    private ApplicationStatus status;
    // ...その他のフィールド

    // ファクトリメソッドによるインスタンス生成
    public static LoanApplication submit(CustomerId customerId, Money amount) {
        if (amount.isLessThanOrEqualTo(Money.ZERO)) {
            throw new IllegalArgumentException("Loan amount must be positive.");
        }
        
        LoanApplication application = new LoanApplication(
            new LoanApplicationId(UUID.randomUUID()),
            customerId,
            amount
        );
        
        // ドメインイベントの登録
        application.registerEvent(new LoanApplicationSubmitted(
            application.getId().getValue(),
            application.getCustomerId().getValue(),
            application.getAmount().getValue()
        ));
        
        return application;
    }
    
    // コンストラクタはprivate/protected
    private LoanApplication(LoanApplicationId id, CustomerId customerId, Money amount) {
        super(id);
        this.customerId = customerId;
        this.amount = amount;
        this.status = ApplicationStatus.SUBMITTED;
    }
    
    // 状態を変更するメソッド
    public void approve() {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Cannot approve application in status: " + this.status);
        }
        this.status = ApplicationStatus.APPROVED;
        registerEvent(new LoanApproved(this.getId().getValue()));
    }
    
    // ゲッターなど
}
```

-   **値オブジェクト (Value Objects)**: `LoanApplicationId`, `CustomerId`, `Money`などは、等価性や不変性を保証する値オブジェクトとして実装します。（`common-ddd`ライブラリに含まれるか、各サービスで定義）

-   **ドメインイベント**:
    -   **`LoanApplicationSubmitted.java`**: 申請が提出されたことを示すイベント。このイベントが、他のマイクロサービス（例: 信用評価サービス）への連携のトリガーとなります。

```java
// application-service/src/main/java/com/example/loan/domain/events/LoanApplicationSubmitted.java
public record LoanApplicationSubmitted(
    UUID loanApplicationId,
    UUID customerId,
    BigDecimal amount
) implements DomainEvent {
}
```

### `LoanApplicationRepository` インターフェース

ドメイン層は、永続化の具体的な方法を知りません。リポジトリの「インターフェース」を定義することで、この関心事を分離します。

```java
// application-service/src/main/java/com/example/loan/domain/LoanApplicationRepository.java
public interface LoanApplicationRepository {
    void save(LoanApplication loanApplication);
    Optional<LoanApplication> findById(LoanApplicationId id);
}
```

---

## 3.2. アプリケーション層とインフラ層の実装

ドメイン層で定義した純粋なビジネスロジックを、外部の世界（Webリクエスト、データベース等）と接続します。

### パッケージ構成

オニオンアーキテクチャの原則に基づき、依存関係が常に内側（ドメイン層）へ向かうようにパッケージを構成します。

```
com.example.loan
├── domain/              # ドメイン層 (依存先なし)
│   ├── model/
│   │   └── LoanApplication.java
│   ├── events/
│   │   └── LoanApplicationSubmitted.java
│   └── LoanApplicationRepository.java
├── application/         # アプリケーション層 (domainに依存)
│   ├── LoanApplicationService.java
│   └── dto/
│       └── SubmitLoanRequest.java
└── infrastructure/      # インフラ層 (domain, applicationに依存)
    ├── web/
    │   └── LoanApplicationController.java
    ├── persistence/
    │   └── MybatisLoanApplicationRepository.java
    └── messaging/
        └── KafkaDomainEventPublisher.java
```

### アプリケーションサービス

ユースケースを実装するクラスです。トランザクションの管理や、リポジトリを介した集約の永続化、イベントの発行などを調整します。

```java
// application-service/src/main/java/com/example/loan/application/LoanApplicationService.java
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public LoanApplicationId submitApplication(SubmitLoanRequest request) {
        LoanApplication application = LoanApplication.submit(
            new CustomerId(request.customerId()),
            new Money(request.amount())
        );
        
        // 1. 集約をDBに保存
        loanApplicationRepository.save(application);
        
        // 2. ドメインイベントを発行
        eventPublisher.publish(application.getDomainEvents());
        
        return application.getId();
    }
}
```

### インフラストラクチャ層

-   **REST API (`LoanApplicationController.java`)**:
    -   Spring Webを使って、外部からのHTTPリクエストを受け付け、アプリケーションサービスを呼び出します。

-   **リポジトリ実装 (`MybatisLoanApplicationRepository.java`)**:
    -   `LoanApplicationRepository`インターフェースを、MyBatisとPostgreSQLを使って実装します。

---

## 3.3. テスト

品質を保証し、リファクタリングを容易にするためには、堅牢なテストが不可欠です。

### 集約の単体テスト (Spock)

ドメインロジックが期待通りに動作することを検証します。外部依存がないため、高速に実行できます。

```groovy
// application-service/src/test/groovy/com/example/loan/domain/LoanApplicationSpec.groovy
class LoanApplicationSpec extends Specification {

    def "should create an application and register a submission event"() {
        given:
        def customerId = new CustomerId(UUID.randomUUID())
        def amount = new Money(new BigDecimal("10000"))

        when:
        def application = LoanApplication.submit(customerId, amount)

        then:
        application.status == ApplicationStatus.SUBMITTED
        application.getDomainEvents().size() == 1
        application.getDomainEvents().get(0) instanceof LoanApplicationSubmitted
    }

    def "should not allow submitting an application with zero or negative amount"() {
        when:
        LoanApplication.submit(new CustomerId(UUID.randomUUID()), new Money(BigDecimal.ZERO))

        then:
        thrown(IllegalArgumentException)
    }
}
```

### リポジトリの結合テスト (Testcontainers)

リポジトリが正しくデータベースとやりとりできるかを検証します。`@MybatisTest`のようなスライス・テストとTestcontainersを組み合わせることで、実際のDB環境に近い形でテストを実行できます。

```java
// application-service/src/test/java/com/example/loan/infrastructure/persistence/MybatisLoanApplicationRepositoryTest.java
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 組み込みDBを無効化
class MybatisLoanApplicationRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LoanApplicationRepository repository;

    @Test
    void shouldSaveAndFindLoanApplication() {
        // given
        LoanApplication application = LoanApplication.submit(...);
        
        // when
        repository.save(application);
        Optional<LoanApplication> found = repository.findById(application.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(application.getId());
    }
}
```

---

これで、最初のマイクロサービスの中核機能が完成しました。次の章では、このサービスが発行したイベントをトリガーに動作する、2つ目のマイクロサービス「信用評価サービス」の実装と、サービス間連携の仕組みを学びます。 