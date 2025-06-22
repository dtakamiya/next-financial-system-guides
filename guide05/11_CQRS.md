# Part 4: 品質編 - 信頼性と保守性の担保

これまでのパートで、私たちはドメインをモデル化し、それを支えるアーキテクチャを設計し、具体的なコードとして実装してきました。最後のPart 4では、システムの品質を長期的に維持し、変化に対応し続けるための高度なパターンと戦略について探求します。

---

# Chapter 11: CQRS - 更新と参照の道を分ける

私たちがこれまで構築してきたシステムでは、状態を変更する「コマンド」処理も、状態を参照する「クエリ」処理も、同じドメインモデル（集約）を使っていました。これは多くのケースでうまく機能しますが、システムが複雑化するにつれて、以下のような問題に直面します。

-   **モデルの不一致**: 更新時にはデータの一貫性を保つための正規化された複雑なモデルが適していますが、参照時には特定の画面表示に最適化された非正規化データの方が高速で便利です。一つのモデルで両方の要求を満たすのは困難です。
-   **パフォーマンスの競合**: 複雑な更新処理と大量の参照処理が同じデータベースにアクセスすると、互いのパフォーマンスを阻害する可能性があります。

**CQRS (Command Query Responsibility Segregation; コマンド・クエリ責務分離)** は、この問題を解決するための強力な建築パターンです。

## 11.1. CQRSの基本概念

CQRSの考え方は非常にシンプルです。それは、**システムの操作を、状態を変更する「コマンド」と、状態を返す「クエリ」の2種類に完全に分離すること**です。

```mermaid
graph TD
    subgraph Command Side (更新系)
        direction LR
        C_API[Command API] --> C_Service[Application Service]
        C_Service --> Agg[Aggregate]
        Agg --> C_Repo[Repository]
        C_Repo --> CmdDB[(Write DB)]
        C_Service -- "DomainEvent" --> Broker[(Message Broker)]
    end
    
    subgraph Query Side (参照系)
        direction LR
        Broker -->|Consumes| Projection[Projection<br>(Event Handler)]
        Projection -->|Writes| QueryDB[(Read DB)]
        Q_API[Query API] --> QueryDB
    end

    Client --> C_API
    Client --> Q_API
    
    style CmdDB fill:#FADBD8,stroke:#E74C3C
    style QueryDB fill:#D6EAF8,stroke:#3498DB
```

-   **コマンド側**:
    -   責務: システムの状態を変更することのみ。
    -   モデル: ビジネスルールと不変条件をカプセル化したドメインモデル（集約）。
    -   データストア: 書き込みに最適化されたデータベース（正規化DB）。
-   **クエリ側**:
    -   責務: システムの状態を返すことのみ。状態変更は一切行わない。
    -   モデル: 特定のUI表示やデータ要件に特化した、非正規化されたデータ構造（リードモデル / DTO）。
    -   データストア: 読み込みに最適化されたデータベース（非正規化DB、リードレプリカ、全文検索エンジンなど）。

## 11.2. シンプルなCQRSの実装ステップ

イベントソーシングを伴わない、より実践的で導入しやすいCQRSの実装方法を見ていきましょう。

### Step 1: リードモデル（参照用テーブル）の設計

まず、「クエリ（参照）」のユースケースに特化したデータストアを設計します。例えば、「ローン申請状況の一覧画面」を表示するためだけに最適化された、以下のような非正規化テーブルをRead DBに作成します。

**テーブル: `query.loan_application_summary`**

| Column                | Description                        | Original Source                  |
| --------------------- | ---------------------------------- | -------------------------------- |
| `application_id` (PK) | 申請ID                             | `LoanApplication` 集約           |
| `customer_name`       | 顧客名                             | `Customer` 集約                  |
| `principal_amount`    | 申請額                             | `LoanApplication` 集約           |
| `application_date`    | 申請日                             | `LoanApplication` 集約           |
| `status`              | 現在のステータス                   | `LoanApplication`, `Underwriting` 集約 |
| `credit_score`        | 信用スコア                         | `CreditReport` 集約              |

このテーブルは、複数の集約にまたがる情報をあらかじめ結合して保持しているため、クエリAPIは単純な`SELECT`文を発行するだけで済みます。

### Step 2: プロジェクション（イベントハンドラ）による同期

次に、コマンド側で発生したドメインイベントを購読し、このリードモデルを常に最新の状態に保つための「プロジェクション」を実装します。

```java
// query-service/src/main/java/com/example/query/projection/LoanApplicationProjection.java
@Component
@RequiredArgsConstructor
public class LoanApplicationProjection {

    private final JdbcTemplate jdbcTemplate;

    // コマンド側の各サービスが発行するイベントをリッスンする
    @KafkaListener(topics = {"loan-applications", "customer-events", "underwriting-events"}, ...)
    public void project(DomainEvent event) {
        // イベントの種類に応じて、リードモデルを更新する
        if (event instanceof LoanApplicationSubmitted e) {
            String sql = "INSERT INTO query.loan_application_summary (application_id, principal_amount, application_date, status) VALUES (?, ?, ?, 'SUBMITTED')";
            jdbcTemplate.update(sql, e.loanApplicationId(), e.amount(), e.occurredOn());
        } 
        else if (event instanceof CustomerNameChanged e) {
            String sql = "UPDATE query.loan_application_summary SET customer_name = ? WHERE customer_id = ?";
            jdbcTemplate.update(sql, e.newName(), e.customerId());
        }
        else if (event instanceof LoanApproved e) {
            String sql = "UPDATE query.loan_application_summary SET status = 'APPROVED' WHERE application_id = ?";
            jdbcTemplate.update(sql, e.loanApplicationId());
        }
        // ... 他のイベントに対応する処理
    }
}
```

### Step 3: クエリ専用APIの作成

最後に、このリードモデルテーブルからデータを取得するためだけの、非常にシンプルなAPIを実装します。

```java
// query-service/src/main/java/com/example/query/api/LoanApplicationQueryController.java
@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
public class LoanApplicationQueryController {

    private final LoanApplicationSummaryDao dao;

    @GetMapping("/applications")
    public List<LoanApplicationSummaryDto> getSummaries(@RequestParam String customerName) {
        // DAOは単純なSELECT文を実行するだけ
        return dao.findByName(customerName);
    }
}
```

## 11.3. トレードオフ：結果整合性

CQRSを導入する上で、最も重要なトレードオフは**結果整合性 (Eventual Consistency)** です。

コマンドが実行されてから、その結果がイベントとして伝搬し、プロジェクションによってリードモデルに反映されるまでには、ミリ秒単位のわずかな遅延が生じます。つまり、ユーザーがデータを更新した直後に同じデータを参照すると、ごく稀に更新前の情報が見えてしまう可能性があります。

多くのWebアプリケーションではこの性質は問題になりませんが、金融取引の残高表示など、即時性が厳密に要求される要件にはCQRSを適用すべきではありません。

---

CQRSは、システムの更新要件と参照要件が非対称である場合に、それぞれの要求に最適化されたソリューションを提供し、パフォーマンス、スケーラビリティ、そして開発の柔軟性を大幅に向上させることができる、強力なアーキテクチャパターンです。 