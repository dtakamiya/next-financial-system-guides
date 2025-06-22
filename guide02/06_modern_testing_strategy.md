# ドメイン駆動設計（DDD）におけるモダンなテスト戦略ガイド

## 1. はじめに

### 1.1. 本ガイドの目的

このガイドは、ドメイン駆動設計（DDD）を採用したプロジェクトにおいて、効果的かつ持続可能なテスト戦略を構築するための指針を提供します。DDDの各要素（エンティティ、値オブジェクト、集約、ドメインサービス、リポジトリ、アプリケーションサービスなど）に対応した、具体的で実践的なテスト手法を解説します。

### 1.2. DDDにおけるテストの重要性

DDDプロジェクトの成功は、ドメインモデルの正確性と堅牢性に大きく依存します。テストは、以下の目的を達成するための不可欠な活動です。

-   **ドメインロジックの正確性の保証**: ビジネスルールが正しく実装されていることを確認します。
-   **リファクタリングの安全性確保**: 複雑なドメインモデルを安心して改善するためのセーフティネットを提供します。
-   **コミュニケーションの促進**: テストコードは、ドメインエキスパートと開発者の共通理解を深めるための「生きたドキュメント」として機能します。
-   **設計へのフィードバック**: 書きにくいテストは、設計の問題点（例：関心の分離が不十分、凝集度が低い）を早期に発見するシグナルとなります。

### 1.3. テスト戦略の基本方針

本ガイドで提唱するテスト戦略は、以下の原則に基づいています。

-   **テストピラミッド/テストダイヤモンドの遵守**: 単体テストを土台としつつ、ドメインの振る舞いを検証する統合テストに重点を置きます。
-   **関心の分離**: テスト対象に応じて、テストの目的と範囲を明確に分離します（例：ドメインロジックのテスト vs インフラストラクチャのテスト）。
-   **振る舞い駆動開発（BDD）の思想の活用**: テストを「仕様」として捉え、ビジネス要件とコードの連携を強化します。
-   **実装の詳細からの独立**: テストは「何を」達成するべきかを検証し、「どのように」実装されているかという詳細に依存しすぎないようにします。

---

## 2. テストの種類とDDDにおける役割

DDDプロジェクトでは、複数のテストレベルを組み合わせることで、品質と開発速度のバランスを取ります。

| テストの種類 | 主な目的 | テスト対象の例 | フレームワーク/ライブラリ |
| :--- | :--- | :--- | :--- |
| **単体テスト (Unit Test)** | 個々のクラスやメソッドのロジックを隔離された環境で検証する。 | 値オブジェクト、エンティティのメソッド、ドメインサービス | JUnit, Mockito, AssertJ |
| **統合テスト (Integration Test)** | 複数のコンポーネントが連携して正しく動作するかを検証する。 | アプリケーションサービス、リポジトリ、ドメインイベント | Spring Boot Test, Testcontainers |
| **E2Eテスト (End-to-End Test)** | 実際のユーザー操作を模倣し、システム全体の振る舞いを検証する。 | UIからデータベースまでの全レイヤー | Cypress, Selenium, Playwright |

### 2.1. テストピラミッドからテストダイヤモンドへ

従来の「テストピラミッド」は、単体テストを最も多く、E2Eテストを最も少なく構成することを推奨します。しかし、DDDにおいては、ドメインロメインの振る舞いを検証する「統合テスト」の価値が非常に高まります。

そのため、本ガイドでは**テストダイヤモンド**の考え方を推奨します。

-   **単体テスト**: 最小限の責務を持つコンポーネント（値オブジェクトなど）に集中させます。
-   **統合テスト**: ドメインの中心的な振る舞い（集約のライフサイクル、アプリケーションサービスのユースケース）を検証する主役とします。
-   **E2Eテスト**: 主要なユーザーストーリーやクリティカルパスに絞って実施します。

![テストダイヤモンド](https://example.com/test-diamond.png)
*(注: 画像はイメージです)*

---

## 3. レイヤーごとのテスト戦略

### 3.1. ドメイン層のテスト

ドメイン層はシステムの心臓部であり、最も重点的にテストする必要があります。

#### 3.1.1. 値オブジェクト (Value Object)

-   **目的**: 不変性、等価性、表明（Assertion）の検証。
-   **テストケース例**:
    -   正常な値でインスタンスが生成できること。
    -   不正な値（null、空文字、業務ルール違反）で生成しようとすると例外がスローされること。
    -   `equals()` と `hashCode()` が値に基づいて正しく判定されること。
    -   不変性が保たれていること（セッターが存在しない、変更しようとすると例外が発生するなど）。

```java
// 例: Email値オブジェクトのテスト
class EmailTest {
    @Test
    void 正しい形式のメールアドレスで生成できる() {
        assertDoesNotThrow(() -> new Email("test@example.com"));
    }

    @Test
    void 不正な形式のメールアドレスでは例外がスローされる() {
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid-email"));
    }

    @Test
    void 同じ値を持つインスタンスは等価である() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");
        assertThat(email1).isEqualTo(email2);
    }
}
```

#### 3.1.2. エンティティ (Entity)

-   **目的**: ライフサイクルを通じた状態変化と、それに伴う不変条件（Invariants）の維持を検証。
-   **テストケース例**:
    -   生成時の初期状態が正しいこと。
    -   ビジネスメソッドを呼び出すと、状態が期待通りに遷移すること。
    -   状態遷移時にドメインイベントが発行されること（もしあれば）。
    -   不正な状態遷移を引き起こす操作は拒否されること（例外スローなど）。
    -   集約のルートエンティティの場合、不変条件が常に維持されていること。

```java
// 例: Orderエンティティのテスト
class OrderTest {
    @Test
    void 商品を追加すると注文明細が増え合計金額が更新される() {
        Order order = new Order(new CustomerId("C123"));
        Product product = new Product(new ProductId("P001"), new Money(1000, "JPY"));

        order.addItem(product, 2); // 2個追加

        assertThat(order.getOrderLines()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(new Money(2000, "JPY"));
    }

    @Test
    void 確定済みの注文に商品を追加しようとすると例外が発生する() {
        Order order = new Order(new CustomerId("C123"));
        order.addItem(new Product(new ProductId("P001"), new Money(1000, "JPY")), 1);
        order.confirm(); // 注文を確定

        assertThrows(IllegalStateException.class, () -> {
            order.addItem(new Product(new ProductId("P002"), new Money(500, "JPY")), 1);
        });
    }
}
```

#### 3.1.3. 集約 (Aggregate)

-   **目的**: トランザクション整合性の境界として、集約内の不変条件が常に守られることを検証。
-   **テスト方針**:
    -   テストは常に**集約のルートエンティティ**を通じて行います。集約内部のエンティティを直接操作するテストは避けるべきです。
    -   1つのテストメソッドで、1つのコマンド（操作）とその結果（状態変化、イベント発行）を検証することに集中します。
    -   **Arrange-Act-Assert (AAA)** パターンを明確に意識します。

#### 3.1.4. ドメインサービス (Domain Service)

-   **目的**: 複数の集約をまたがるドメインロジックの検証。
-   **テスト方針**:
    -   ドメインサービスが依存するリポジトリや他のサービスは、**モック**に置き換えます。
    -   モックを用いて、特定の条件下でドメインサービスがどのように振る舞うかをテストします。

```java
// 例: 振込ドメインサービスのテスト
@ExtendWith(MockitoExtension.class)
class MoneyTransferServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private MoneyTransferService moneyTransferService;

    @Test
    void 振込に成功すると送金元口座の残高が減り送金先口座の残高が増える() {
        // Arrange
        Account fromAccount = new Account(new AccountId("A001"), new Money(10000, "JPY"));
        Account toAccount = new Account(new AccountId("A002"), new Money(5000, "JPY"));
        when(accountRepository.findById(new AccountId("A001"))).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(new AccountId("A002"))).thenReturn(Optional.of(toAccount));

        // Act
        moneyTransferService.transfer(new AccountId("A001"), new AccountId("A002"), new Money(3000, "JPY"));

        // Assert
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(accountCaptor.capture());

        List<Account> savedAccounts = accountCaptor.getAllValues();
        assertThat(savedAccounts.get(0).getBalance()).isEqualTo(new Money(7000, "JPY")); // From
        assertThat(savedAccounts.get(1).getBalance()).isEqualTo(new Money(8000, "JPY")); // To
    }
}
```

### 3.2. アプリケーション層のテスト

アプリケーション層は、ユースケースを実現し、ドメイン層とインフラストラクチャ層を協調させる役割を担います。

#### 3.2.1. アプリケーションサービス (Application Service)

-   **目的**: 1つのユースケースが正しく完遂されることの検証。
-   **テスト方針**:
    -   これは**統合テストの主戦場**です。
    -   データベースや外部APIなど、インフラ層のコンポーネントを含めてテストします。
    -   Spring Bootを利用している場合、`@SpringBootTest` を活用します。
    -   永続化を含むテストでは、**Testcontainers** を使用して、本番環境と同一のバージョンのデータベース（PostgreSQL, MySQLなど）をテスト実行時に起動することを強く推奨します。これにより、インメモリDB（H2など）との方言の差異による問題を回避できます。
    -   各テストメソッドの実行後、データベースの状態が次のテストに影響を与えないように、トランザクションをロールバックするか、データをクリーンアップする仕組みが必要です（例: `@Transactional` アノテーション）。

```java
// 例: ユーザー登録アプリケーションサービスのテスト
@SpringBootTest
@Transactional // テスト後にDBをロールバック
class UserRegistrationServiceTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void ユーザーを正常に登録できる() {
        // Arrange
        UserRegistrationCommand command = new UserRegistrationCommand("test-user", "password123", "test@example.com");

        // Act
        UserId newUserId = userRegistrationService.registerUser(command);

        // Assert
        Optional<User> foundUser = userRepository.findById(newUserId);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("test-user");
    }

    @Test
    void 同じユーザー名が既に存在する場合は例外をスローする() {
        // Arrange
        // 事前に同じ名前のユーザーを登録しておく
        userRepository.save(new User(new UserId(), "existing-user", "hashed-password", "email@test.com"));
        UserRegistrationCommand command = new UserRegistrationCommand("existing-user", "password123", "test@example.com");

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userRegistrationService.registerUser(command);
        });
    }
}
```

### 3.3. インフラストラクチャ層のテスト

インフラストラクチャ層は、外部システムとの接続を担当します。

#### 3.3.1. リポジトリ (Repository)

-   **目的**: ORM（JPAなど）のマッピングやクエリが正しく動作することの検証。
-   **テスト方針**:
    -   アプリケーションサービスのテストと同様に、**Testcontainers** を用いた実データベースに対する統合テストとして実装します。
    -   `@DataJpaTest` などのスライステスト用アノテーションを利用すると、リポジトリ層のテストに特化したコンテキストを効率的に読み込めます。
    -   複雑なクエリメソッド（Specification, JPQL, QueryDSLなど）は、個別にテストケースを用意します。

```java
// 例: UserRepositoryのテスト
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 組込みDBを無効化
@ContextConfiguration(initializers = TestcontainersInitializer.class) // Testcontainersを初期化
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByStatusOrderByRegistrationDateDescで特定のステータスのユーザーを取得できる() {
        // Arrange (テストデータのセットアップ)
        userRepository.save(new User(..., UserStatus.ACTIVE, ...));
        userRepository.save(new User(..., UserStatus.INACTIVE, ...));
        userRepository.save(new User(..., UserStatus.ACTIVE, ...));

        // Act
        List<User> activeUsers = userRepository.findByStatusOrderByRegistrationDateDesc(UserStatus.ACTIVE);

        // Assert
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).allMatch(user -> user.getStatus() == UserStatus.ACTIVE);
        // 登録日時の降順になっているかも検証
    }
}
```

#### 3.3.2. 外部APIクライアント

-   **目的**: 外部APIとの通信（リクエスト、レスポンス）が正しく行えることの検証。
-   **テスト方針**:
    -   **WireMock** や **Hoverfly** などのHTTPサーバーモックライブラリを使用します。
    -   これにより、実際の外部APIに接続することなく、様々なレスポンスパターン（正常系、異常系、タイムアウトなど）をシミュレートできます。
    -   ネットワーク障害などに依存しない、安定したテストが可能になります。

---

## 4. テスト実装のベストプラクティス

### 4.1. テストデータの管理

-   **Object Mother パターン / Test Data Builder パターン**: テストデータの生成を責務とするクラスを用意し、テストコードの可読性と再利用性を高めます。
-   **ファイルからの読み込み**: 大量のマスターデータなどが必要な場合は、CSVやJSONファイルからテストデータを読み込むと、テストコードの見通しが良くなります。

### 4.2. 表明（Assertion）の書き方

-   **AssertJ の活用**: `assertThat(actual).isEqualTo(expected)` のように流れるように記述できるため、可読性が向上します。JUnit標準のアサーションよりも推奨されます。
-   **ドメイン固有の表明**: `assertThat(order).isConfirmed()` のような、ドメインの言語に合わせたカスタムアサーションを定義すると、テストの意図がより明確になります。

### 4.3. BDD（振る舞い駆動開発）スタイルのテスト記述

-   **Given-When-Then**: テストの構造を「前提条件(Given)」「操作(When)」「期待される結果(Then)」の3つのセクションに分けることで、テストの目的が明確になります。
-   **テストメソッドの命名**: 「`should` + 期待される振る舞い」や日本語で「`～の場合～となること`」のように、テストの仕様がわかる名前にします。

---

## 5. まとめ

DDDプロジェクトにおける成功は、ドメインモデルの品質にかかっています。そして、その品質を支え、進化を可能にするのが、本ガイドで紹介した多層的で実践的なテスト戦略です。

-   **ドメイン層**は、単体テストでロジックの正しさを保証します。
-   **アプリケーション層**は、実データベース（Testcontainers）を使った統合テストで、ユースケースの完全性を検証します。
-   **インフラストラクチャ層**は、スライステストやHTTPモックを使い、外部世界との接続を確実にします。

この戦略を実践することで、開発チームは自信を持ってリファクタリングを行い、ビジネスの変化に迅速に対応できる、真に価値のあるソフトウェアを構築し続けることができるでしょう。 