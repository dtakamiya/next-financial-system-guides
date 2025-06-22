# Chapter 4: 信用評価マイクロサービス(Scoring Service)の実装とサービス間連携

前の章で実装した「申請受付サービス」は、申請を受け付けると`LoanApplicationSubmitted`イベントを発行します。本章では、このイベントを購読して動作する2つ目のマイクロサービス「信用評価サービス」を構築します。イベント駆動によるサービス間連携と、外部APIとの連携テストが主なテーマです。

## 4.1. イベント駆動による連携

信用評価サービスは、`LoanApplicationSubmitted`イベントをリッスンすることから始まります。これにより、申請受付サービスと信用評価サービスは直接結合することなく、疎に連携できます。

### Kafkaイベントリスナーの実装

Spring for Apache Kafkaを利用して、特定のトピックを購読するリスナーを簡単に実装できます。

```java
// scoring-service/src/main/java/com/example/scoring/infrastructure/messaging/LoanApplicationEventsListener.java
@Component
@RequiredArgsConstructor
public class LoanApplicationEventsListener {

    private final ScoringService scoringService;

    // "loan-applications"トピックをリッスンする
    @KafkaListener(topics = "loan-applications", groupId = "scoring-group")
    public void handleLoanApplicationSubmitted(LoanApplicationSubmitted event) {
        // イベントペイロードを受け取り、アプリケーションサービスを呼び出す
        scoringService.calculateScore(event.customerId());
    }
}

// イベントクラスは共通ライブラリか、各サービスで定義を共有
// common-events/src/main/java/com/example/events/LoanApplicationSubmitted.java
public record LoanApplicationSubmitted(
    UUID loanApplicationId,
    UUID customerId,
    BigDecimal amount
) {
}
```

---

## 4.2. ドメイン層の実装

信用評価サービスのドメイン層は、「顧客の信用度を評価する」という自身の責務に集中します。

### `CreditReport` 集約

-   **`CreditReport.java`**:
    -   顧客の信用情報を表す集約。信用スコアや過去の債務履歴などを保持します。
    -   `calculateScore()`ファクトリメソッドが、外部の信用情報機関から取得したデータに基づいてスコアを計算するロジックをカプセル化します。

```java
// scoring-service/src/main/java/com/example/scoring/domain/CreditReport.java
public class CreditReport extends AggregateRoot<CreditReportId> {

    private final CustomerId customerId;
    private final int score;
    // ...

    public static CreditReport calculate(CustomerId customerId, CreditBureauData externalData) {
        // 外部データからスコアを計算する複雑なビジネスロジック
        int calculatedScore = performCalculation(externalData);
        
        CreditReport report = new CreditReport(..., customerId, calculatedScore);
        
        // 評価完了イベントを発行
        report.registerEvent(new CreditScoreCalculated(
            report.getId().getValue(),
            report.getCustomerId().getValue(),
            report.getScore()
        ));
        
        return report;
    }
    // ...
}
```

### 外部APIのためのポートとアダプター

ドメイン層は、外部API（この場合は信用情報機関）の具体的な存在を知るべきではありません。DDDでは、これを「ポート（インターフェース）」として定義し、具体的な実装（アダプター）をインフラストラクチャ層に配置します。

-   **ポート (インターフェース)**:
    ```java
    // scoring-service/src/main/java/com/example/scoring/domain/port/CreditBureau.java
    public interface CreditBureau {
        CreditBureauData fetchCreditData(CustomerId customerId);
    }
    ```

-   **アダプター (実装)**:
    ```java
    // scoring-service/src/main/java/com/example/scoring/infrastructure/gateway/HttpCreditBureau.java
    @Component
    public class HttpCreditBureau implements CreditBureau {
        // RestTemplateやWebClientを使って外部APIを呼び出す実装
    }
    ```

---

## 4.3. テスト戦略

サービス間連携や外部API呼び出しを含むテストは複雑になりがちです。Testcontainersは、このような依存関係をテスト実行時にコンテナとして起動・管理することで、信頼性の高いテストを可能にします。

### 外部APIのモック化 (WireMock / MockServer)

外部の信用情報機関APIは、テストの度に呼び出すべきではありません（コスト、不安定さの問題）。WireMockやMockServerのようなツールをTestcontainers経由で利用し、APIの振る舞いをシミュレートします。

```groovy
// scoring-service/src/test/groovy/com/example/scoring/application/ScoringServiceIntegrationSpec.groovy
class ScoringServiceIntegrationSpec extends Specification {

    // MockServerコンテナを定義
    static MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))

    def setupSpec() {
        mockServer.start()
        // MockServerに期待するリクエストとレスポンスを設定
        new MockServerClient(mockServer.host, mockServer.serverPort)
            .when(
                request().withMethod("GET").withPath("/credit-reports/.*"),
                exactly(1)
            )
            .respond(
                response().withStatusCode(200).withBody('{"score": 750, ...}')
            )
    }
    
    // ... テストコード ...
}
```

### イベント送受信のテスト (Testcontainers for Kafka)

アプリケーションがKafkaからイベントを正しく受信し、処理を開始できるかを検証します。

```java
// scoring-service/src/test/java/com/example/scoring/infrastructure/messaging/LoanApplicationEventsListenerTest.java
@SpringBootTest
@EmbeddedKafka // Spring Test for Kafkaが提供する組み込みKafkaを利用
class LoanApplicationEventsListenerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private ScoringService scoringService;

    @Test
    void shouldHandleLoanApplicationSubmittedEvent() {
        // given: テスト用のイベントを作成
        LoanApplicationSubmitted event = new LoanApplicationSubmitted(...);

        // when: "loan-applications"トピックにイベントを送信
        kafkaTemplate.send("loan-applications", event);

        // then: イベントが消費され、ScoringServiceのメソッドが呼び出されることを検証
        // Awaitilityなどを使って非同期処理を待機
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(scoringService).calculateScore(event.customerId());
        });
    }
}
```

---

この章では、イベント駆動アーキテクチャの中核となる、非同期でのサービス間連携を実装しました。また、外部APIという不確実な要素をTestcontainersでコントロールし、安定したテストを実行するテクニックを学びました。

次の章では、複数のマイクロサービスにまたがるビジネスプロセスを管理するための「Sagaパターン」を実装します。 