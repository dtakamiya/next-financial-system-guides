# 第1章: ドメインイベントの核心

## 1.1. ドメインイベントとは？

**ドメインイベント**とは、ドメイン（ビジネス領域）内で過去に発生した、ビジネス上意味のある**「出来事」**を記録したものです。それは「事実」であり、一度発生したら変わることはありません。

ドメイン駆動設計（DDD）の提唱者であるエリック・エヴァンスは、ドメインイベントを「ドメインエキスパートが追跡したり通知を受けたりしたいイベント、あるいは他のモデルオブジェクトの状態変化に関連するイベント」と定義しています。これは、単なる技術的なログではなく、ビジネスの関心事と密接に結びついた、ドメインモデルの重要な一部であることを意味します。

### ドメインイベントの主な特徴

-   **過去形の命名**: イベントが過去の事実であることを示すため、常に「〜済み（...ed）」という過去形で命名されます。（例: `OrderPlaced`, `PasswordChanged`）
-   **不変性 (Immutable)**: 一度発生したイベントの内容は変更できません。これにより、信頼できる履歴記録としての価値が保証されます。
-   **関連データの保持**: イベントの購読者が必要とする、出来事に関する十分な情報（何が、いつ、なぜ起こったか）を保持します。
-   **ビジネス上の重要性**: 技術的なシステムイベント（例: DB行更新）ではなく、ビジネスにとって意味のある出来事を表します。

```java
// 「注文確定」ドメインイベントの例
public final class OrderConfirmed {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Instant occurredOn;

    public OrderConfirmed(OrderId orderId, CustomerId customerId) {
        this.orderId = Objects.requireNonNull(orderId);
        this.customerId = Objects.requireNonNull(customerId);
        this.occurredOn = Instant.now(); // イベント発生日時
    }

    // Getterのみを公開し、Setterは持たないことで不変性を保つ
    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
```

### 「イベント」と「コマンド」の違い

-   **コマンド (Command)**: これから何かを行うようにという**「要求」**です。（例: `PlaceOrder` コマンド）
-   **イベント (Event)**: 何かが既に**「実行された」**という**「通知」**です。（例: `OrderPlaced` イベント）

---

# 第2章: なぜドメインイベントが重要なのか？

ドメインイベントを導入することは、システム設計に多くのメリットをもたらします。

## 2.1. コンポーネント間の疎結合の実現

ドメインイベントは、システムの異なる部分（コンポーネントやマイクロサービス）間の直接的な依存関係を劇的に減らします。

-   イベント発行者は、単に「これが起こった」と通知するだけです。
-   イベント購読者（サブスクライバー）は、発行元を意識することなく、関心のあるイベントをリッスンして処理を実行します。

この**疎結合**な関係により、あるコンポーネントの変更が他のコンポーネントに影響を与える可能性が低くなり、システムの変更容易性、拡張性、保守性が大幅に向上します。

## 2.2. 結果整合性の実現

複数のアグリゲートやサービスにまたがるビジネスプロセスにおいて、ドメインイベントは**結果整合性（Eventual Consistency）**を実現するための強力な手段となります。

ある操作が完了した際にイベントを発行し、そのイベントを購読した他のコンポーネントが非同期で後続処理を行うことで、システム全体で即時的な強い整合性を保つ必要がなくなります。これにより、トランザクションの範囲を小さく保ち、システムのパフォーマンスとスケーラビリティを向上させることができます。

## 2.3. ビジネスプロセスの可視化

一連のドメインイベントは、ビジネスプロセスのステップを明確に示します。

-   例: `OrderPlaced` → `PaymentProcessed` → `OrderConfirmed` → `ShipmentInitiated`

このようにイベントを時系列に並べることで、ビジネスプロセス全体を可視化し、ドメインエキスパートと開発者の共通理解を促進します。

---

# 第3章: 効果的なドメインイベントの設計

## 3.1. イベントが運ぶべき情報（イベントペイロード）

イベントは、購読者が基本的な処理を行えるだけの十分なデータを含むべきです。これにより、購読者がイベント発行元に何度も問い合わせる（チャッティになる）必要がなくなり、自律性が高まります。

-   **含めるべき情報の例**:
    -   関連エンティティの識別子（例: `OrderID`, `CustomerID`）
    -   変更に関連する主要なデータポイント（例: `ProductPriceChanged` なら商品ID、旧価格、新価格）
    -   イベント発生時のタイムスタンプ

ただし、イベントに関係のない過剰な情報を含めることは避け、必要最小限のデータに留めるバランスが重要です。

## 3.2. イベントの粒度：大きすぎず、小さすぎず

イベントの粒度は、ビジネス上の意味を考慮して適切に定義する必要があります。

-   **広すぎるイベントは避ける**: `OrderUpdated` のような曖昧なイベントは、何が変更されたのか分からず、購読者の処理を複雑にします。
-   **細かすぎるイベントも避ける**: ビジネス上重要でない限り、すべての小さな属性変更に対してイベントを発行する必要はありません。（例: `ShippingAddressStreetChanged`, `ShippingAddressCityChanged` ではなく `ShippingAddressChanged`）

## 3.3. イベント生成における集約の役割

ドメインイベントは、通常**アグリゲート（Aggregate）**から発行されます。

コマンドを受け取ったアグリゲートが、その内部状態を変更し、ビジネスルール（不変条件）を検証した結果として、ドメインイベントを生成・発行するのが一般的なパターンです。これにより、イベントが常に一貫性の保たれた正しい状態から発行されることが保証されます。

```java
public abstract class AggregateRoot {
    
    // アグリゲート内で発生したイベントを一時的に保持するリスト
    private final transient List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    public List<Object> pollDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}

public class Order extends AggregateRoot {

    private OrderId id;
    private CustomerId customerId;
    private OrderStatus status;
    // ... 他のプロパティ

    // 注文を確定するビジネスロジック
    public void confirm() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("下書き状態の注文のみ確定できます。");
        }
        this.status = OrderStatus.CONFIRMED;

        // 状態変更の結果としてドメインイベントを生成し、登録する
        this.registerEvent(new OrderConfirmed(this.id, this.customerId));
    }
}
```

---

# 第4章: イベントの発行と購読の仕組み

ドメインイベントは、以下の流れでシステム内を伝播します。

1.  **イベントの生成と発行**:
    -   アグリゲートがコマンドを処理し、状態を変更した結果、ドメインイベントを生成します。
    -   生成されたイベントは、アグリゲート内部のリストなどに一時的に保持されます。
    -   トランザクションがコミットされる直前（または直後）に、**イベントディスパッチャー**などの仕組みを通じてイベントが発行（Dispatch）されます。

2.  **イベントの購読と処理**:
    -   **イベントハンドラ**（またはサブスクライバー）が、自身が関心を持つ特定のドメインイベントを購読します。
    -   イベントが発行されると、ディスパッチャーが対応するイベントハンドラを呼び出します。
    -   イベントハンドラは、受け取ったイベントの情報を使って、メール送信、別のアグリゲートの更新、外部システムとの連携など、具体的な処理を実行します。

この発行・購読モデル（Pub/Subモデル）は、同期的にも非同期（メッセージキューなどを介して）にも実装可能であり、システムの要件に応じて柔軟な連携を実現します。

```java
// --- アプリケーションサービス層 ---
@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    // ...

    @Transactional
    public void confirmOrder(OrderId orderId) {
        // 1. アグリゲートのメソッドを呼び出す
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.confirm();
        
        // 2. アグリゲートを保存する
        orderRepository.save(order);

        // 3. アグリゲートが保持するイベントを発行する
        //    (この処理はリポジトリのsaveメソッド内やAOPで自動化されることも多い)
        eventPublisher.publish(order.pollDomainEvents());
    }
}

// --- インフラストラクチャ層 or ドメインサービス ---
@Component
public class DomainEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher; // Springのイベント発行機能を利用

    public void publish(List<Object> events) {
        events.forEach(applicationEventPublisher::publishEvent);
    }
}

// --- イベントハンドラ（別のドメインやアプリケーション層） ---
@Component
public class OrderConfirmedHandler {

    private final MailService mailService;

    // ...

    @EventListener
    public void handle(OrderConfirmed event) {
        // イベントを受け取り、副作用（メール送信など）を実行する
        mailService.sendConfirmationEmail(event.getCustomerId(), event.getOrderId());
    }
} 