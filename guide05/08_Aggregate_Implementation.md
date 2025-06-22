# Chapter 8: 集約（Aggregate）の実装 - ビジネスルールを守る城壁

これまでの章で、私たちはドメインモデルの重要性、そしてそれを守るためのアーキテクチャについて学んできました。この章では、その城壁の内側で最も重要な存在、**集約（Aggregate）**を実際にコードで構築する方法を学びます。

集約は単なるデータの入れ物ではありません。それは、**ビジネスルールを強制し、データの一貫性を絶対に守り抜く、ドメインの守護者**です。

## 8.1. なぜ集約が「最重要」なのか？

-   **一貫性の境界**: アグリゲートは、関連するオブジェクト群（エンティティ、値オブジェクト）が常にビジネスルール上正しい状態であることを保証する範囲を定義します。「注文合計金額と明細の合計は必ず一致する」といったルールは、`Order`アグリゲートの責任です。
-   **トランザクションの単位**: データベースへの保存・更新は、アグリゲート単位で行うのが原則です。これにより、アグリゲート内の整合性が常に保たれます。
-   **カプセル化の要**: アグリゲートの内部構造は外部から隠蔽されます。外部からのアクセスは、必ず**アグリゲートルート**という唯一の窓口を通して行われなければなりません。

## 8.2. 実装のベストプラクティス：5つの黄金律

堅牢なアグリゲートを実装するためには、以下の5つのルールを徹底することが極めて重要です。ここでは、`guide04`で設計した「ローン申請」(`LoanApplication`)を例に解説します。

---

### 黄金律1：コンストラクタで「最初の不変条件」を強制する

アグリゲートは、生成された瞬間に「不正な状態」であってはなりません。コンストラクタやファクトリメソッドは、オブジェクトが有効な状態で生成されることを保証する最初の砦です。

```java
// domain/model/loan/LoanApplication.java

public class LoanApplication {
    private LoanApplicationId id;
    private Money principal; // 融資希望額
    private ApplicationStatus status;
    // ... other fields

    // ファクトリメソッド
    public static LoanApplication apply(CustomerId customerId, Money principal) {
        // 不変条件チェック：融資希望額はゼロより大きくなければならない
        if (principal.isLessThanOrEqualTo(Money.ZERO_JPY)) {
            throw new IllegalArgumentException("Principal must be greater than zero.");
        }
        
        LoanApplication application = new LoanApplication();
        application.id = LoanApplicationId.generate();
        application.principal = principal;
        application.status = ApplicationStatus.SUBMITTED;
        
        // ★ドメインイベントを発行して「申請された」という事実を記録
        application.registerEvent(new LoanApplicationSubmitted(application.id, customerId, principal));
        
        return application;
    }

    private LoanApplication() { /* ORMのための空のコンストラクタ */ }
}
```

### 黄金律2：セッターを公開しない (Tell, Don't Ask)

アグリゲートの状態を外部から自由に書き換えられるように`public`なセッターを公開することは、カプセル化を破壊する最悪の行為です。状態を変更したい場合は、その意図を表すビジネスメソッド（次のルールを参照）を呼び出します。

```java
// アンチパターン：誰でも状態を書き換えられる
public class LoanApplication {
    private ApplicationStatus status;
    public void setStatus(ApplicationStatus status) { // ★絶対ダメ！
        this.status = status;
    }
}

// 良い例：状態は内部でのみ管理される
public class LoanApplication {
    private ApplicationStatus status;
    public ApplicationStatus getStatus() { // ゲッターはOK
        return this.status;
    }
}
```

### 黄金律3：ビジネスロジックをメソッドとして実装する

「貧血ドメインモデル」を避け、アグリゲートを豊かにするため、ビジネスの振る舞いをアグリゲート自身のメソッドとして実装します。これらのメソッドは、状態を変更する唯一の手段となります。

```java
// domain/model/loan/LoanApplication.java

public class LoanApplication {
    // ... fields
    
    public void approve(審査員Id reviewerId) {
        // 不変条件チェック：申請中のステータスでなければ承認できない
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Cannot approve an application that is not in SUBMITTED state.");
        }
        this.status = ApplicationStatus.APPROVED;
        
        // ★ドメインイベントを発行
        this.registerEvent(new LoanApplicationApproved(this.id, reviewerId, Instant.now()));
    }
    
    public void reject(審査員Id reviewerId, String reason) {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Cannot reject an application that is not in SUBMITTED state.");
        }
        this.status = ApplicationStatus.REJECTED;
        
        // ★ドメインイベントを発行
        this.registerEvent(new LoanApplicationRejected(this.id, reviewerId, reason, Instant.now()));
    }

    // ...
}
```
`ApplicationService`の役割は、`LoanApplication`アグリゲートを見つけてきて、`approve()`や`reject()`といったメソッドを呼び出すことです。ロジックそのものはアグリゲート内に存在します。

### 黄金律4：内部のコレクションは「変更不可」で返す

アグリゲートが内部にエンティティのリスト（例：注文明細）を持つ場合、そのリストを返すゲッターは、リストの**変更不可能なビュー**を返さなければなりません。これにより、外部からリストに要素が勝手に追加・削除されることを防ぎます。

```java
public class OrderAggregate {
    private final List<OrderLine> orderLines = new ArrayList<>();

    // 良くない例：内部リストが外部から変更されてしまう
    public List<OrderLine> getOrderLines_BAD() {
        return this.orderLines;
    }

    // 良い例：変更不可能なラッパーで包んで返す
    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(this.orderLines);
    }
    
    // 内部状態を変更するための専用メソッド
    public void addLine(Product product, int quantity) {
        // ... 不変条件チェックなど ...
        this.orderLines.add(new OrderLine(product, quantity));
    }
}
```

### 黄金律5：状態変更の結果としてドメインイベントを発行する

アグリゲートの状態が変化したとき、それはビジネス上意味のある「出来事」が発生したことを意味します。この出来事を**ドメインイベント**として記録し、発行する責務をアグリゲートに持たせます。

```java
// すべてのアグリゲートの基底クラス
public abstract class AggregateRoot {
    
    private final transient List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }
    
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

// LoanApplicationはこれを継承する
public class LoanApplication extends AggregateRoot {
    // ...
}
```
> Spring Dataの `@DomainEvents` と `@AfterDomainEventPublication` を使うと、リポジトリが `save()` を呼び出す際に、自動的にイベントを発行し、その後リストをクリアしてくれます。

---

これらの5つの黄金律に従うことで、あなたのアグリゲートは、ビジネスの複雑さに立ち向かうための、信頼できる堅牢なコードの塊となります。

次の章では、このアグリゲートを実際に永続化し、データベースとの間でやり取りするための**リポジトリ**の実装について見ていきます。 