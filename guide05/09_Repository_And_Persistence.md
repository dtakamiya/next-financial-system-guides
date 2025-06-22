# Chapter 9: リポジトリと永続化 - ドメインとデータベースを繋ぐ橋

前の章で、私たちはビジネスルールをカプセル化した堅牢な「集約」を実装しました。しかし、この集約オブジェクトはメモリ上に存在するだけでは、アプリケーションを再起動すると消えてしまいます。これを恒久的に保存（永続化）し、後で再取得するための仕組みが必要です。

ここで登場するのが**リポジトリ (Repository)** パターンです。リポジトリは、ドメインの世界とデータベースの世界の間に立ち、両者の間の「通訳」として機能します。

## 9.1. リポジトリの責務

-   **永続化の抽象化**: ドメイン層に対して、まるでオブジェクトがメモリ上のコレクションであるかのように見せかけます。ドメイン層は、背後にあるのがPostgreSQLなのか、MySQLなのか、あるいはJPAなのかMyBatisなのか、一切知る必要がありません。
-   **集約のライフサイクル管理**: リポジトリは、集約をデータベースから取得（再構築）し、メモリ上での変更をデータベースに保存する責務を持ちます。
-   **依存性逆転の鍵**: リポジトリは、クリーンアーキテクチャを支える「依存性逆転の原則」の最も代表的な実践例です。

## 9.2. 実装の2ステップ

リポジトリの実装は、常に以下の2つのステップで行われます。

### Step 1: ドメイン層で「契約（インターフェース）」を定義する

リポジトリがどのような振る舞いを提供すべきか、その「契約」はドメイン層が決定します。なぜなら、何が必要かを知っているのは、それを利用するドメイン層自身だからです。

```java
// domain/model/loan/LoanApplicationRepository.java
package com.example.ddd.domain.model.loan;

import java.util.Optional;

// これはドメイン層に配置される
public interface LoanApplicationRepository {
    
    Optional<LoanApplication> findById(LoanApplicationId id);
    
    void save(LoanApplication loanApplication);
    
    // 必要であれば、他の検索メソッドも定義できる
    // List<LoanApplication> findByStatus(ApplicationStatus status);
}
```

### Step 2: インフラ層で「契約（インターフェース）」を実装する

実際のデータベース操作を行う実装クラスは、インフラストラクチャ層に配置します。これにより、ソースコードの依存関係は `インフラ層 -> ドメイン層` となり、クリーンアーキテクチャのルールが守られます。

ここでは、Spring Data JPAを使った実装例を見てみましょう。

```java
// infrastructure/persistence/LoanApplicationRepositoryImpl.java
package com.example.ddd.infrastructure.persistence;

// ... imports

@Repository // Springのコンポーネントとして登録
public class LoanApplicationRepositoryImpl implements LoanApplicationRepository {

    private final JpaLoanApplicationRepository jpaRepository; // Spring Data JPAが自動生成するリポジトリ
    private final LoanApplicationMapper mapper; // ドメインモデルとJPAエンティティを変換するマッパー

    public LoanApplicationRepositoryImpl(JpaLoanApplicationRepository jpaRepository, LoanApplicationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<LoanApplication> findById(LoanApplicationId id) {
        return jpaRepository.findById(id.getValue())
                            .map(mapper::toDomain); // JpaEntity -> DomainModel
    }

    @Override
    public void save(LoanApplication loanApplication) {
        LoanApplicationJpaEntity entity = mapper.toEntity(loanApplication); // DomainModel -> JpaEntity
        jpaRepository.save(entity);
    }
}

// Spring Data JPAのインターフェース
interface JpaLoanApplicationRepository extends JpaRepository<LoanApplicationJpaEntity, UUID> {
}
```

## 9.3. 最重要プラクティス：ドメインモデルと永続化モデルの分離

上記のコード例で `LoanApplicationJpaEntity` や `LoanApplicationMapper` が登場したことに気づいたでしょうか。これは極めて重要なプラクティスです。

**問題点**:
`LoanApplication` ドメインモデルのクラスに、`@Entity` や `@Table`, `@Column` といったJPAのアノテーションを直接記述すると、ドメインモデルが特定の永続化技術（JPA）に汚染されてしまいます。これは、ドメイン層の純粋性を損ない、クリーンアーキテクチャの原則に反します。

**解決策**:
永続化のためだけのプレーンなクラス（`LoanApplicationJpaEntity`）をインフラ層に作成し、ドメインモデルとの間で相互に変換（マッピング）する責務をマッパーに持たせます。

```java
// infrastructure/persistence/LoanApplicationJpaEntity.java
@Entity
@Table(name = "loan_applications")
public class LoanApplicationJpaEntity {
    @Id
    private UUID id;
    private BigDecimal principal;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    // ... JPAのためのゲッター、セッター
}

// infrastructure/persistence/LoanApplicationMapper.java
@Component
public class LoanApplicationMapper {
    public LoanApplication toDomain(LoanApplicationJpaEntity entity) {
        // ... entityのフィールドを使ってLoanApplicationドメインオブジェクトを再構築する
    }

    public LoanApplicationJpaEntity toEntity(LoanApplication domain) {
        // ... domainオブジェクトのフィールドを使ってJpaEntityを作成する
    }
}
```
この一手間をかけることで、ドメインモデルは永続化の懸念から完全に解放され、純粋なビジネスロジックに集中できるようになります。将来、永続化技術をJPAからMyBatisや他のものに変更したくなったとしても、修正範囲はインフラストラクチャ層に限定されます。

## 9.4. リポジトリのテスト戦略

リポジトリのテストは、データベースとの実際のやり取りを検証する「結合テスト」です。ここでは、インメモリデータベース（H2など）ではなく、**Testcontainers** を使って、本番環境と同じ種類のデータベース（この場合はPostgreSQL）のコンテナをテスト実行時に起動することを強く推奨します。

```groovy
// infrastructure/persistence/LoanApplicationRepositoryImplSpec.groovy
@SpringBootTest
class LoanApplicationRepositoryImplSpec extends Specification {

    @Autowired
    private LoanApplicationRepository repository;

    // TestcontainersがPostgreSQLコンテナを起動・管理してくれる
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")

    // コンテナの接続情報をSpringのデータソース設定に動的に反映する
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }

    def "should save and find a loan application"() {
        given: "新しいローン申請の集約オブジェクトを作成"
        def application = LoanApplication.apply(
            new CustomerId(UUID.randomUUID()), 
            new Money(new BigDecimal("50000"), "JPY")
        )

        when: "リポジトリを使って保存する"
        repository.save(application)

        and: "同じIDで再取得する"
        def found = repository.findById(application.getId())

        then: "正しく取得でき、状態が一致すること"
        found.isPresent()
        found.get().getId() == application.getId()
        found.get().getStatus() == ApplicationStatus.SUBMITTED
    }
}
```
これにより、「ローカルのH2では動いたのに、本番のPostgreSQLでは動かなかった」といった問題を未然に防ぐことができます。

---
リポジトリは、ドメインとインフラストラクチャを綺麗に分離するための、シンプルかつ強力なパターンです。このパターンを正しく適用することで、あなたのドメインモデルは永続化という厄介な問題から解放され、真にビジネス価値の創造に集中できるようになります。 