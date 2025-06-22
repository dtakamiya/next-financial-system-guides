# Chapter 13: モダンなテスト戦略 - 品質の守護神

## 1. はじめに: なぜDDDにおけるテストは特別なのか

ドメイン駆動設計（DDD）におけるテストは、単なる品質保証活動ではありません。それは、ドメインモデルの正確性を検証し、ビジネス要件をコードに反映させるための、設計プロセスそのものに深く組み込まれた活動です。

-   **設計ツールとしてのテスト**: DDDのテストは「ビジネスとして何をすべきか」を検証します。これにより、テストは実装の詳細から分離され、リファクタリングに強い、価値ある資産となります。テストが書きにくい場合、それはドメインモデル自体の設計に問題があることを示す重要なフィードバックとなります。

-   **生きたドキュメントとしてのテスト**: ユビキタス言語を用いて記述されたテストは、ドメインエキスパートも理解できる「実行可能な仕様書」となります。`given-when-then`のようなBDD（振る舞い駆動開発）スタイルは、この思想と非常に親和性が高いです。

-   **迅速なフィードバックループ**: 自動化されたテストスイートは、モデルへの変更が既存のビジネスルールを破壊していないことを即座にフィードバックします。これにより、開発チームは自信を持ってモデルを継続的に改善できます。

## 2. テスト容易性を実現するアーキテクチャ

効果的なテストは、テスト容易性の高いアーキテクチャから生まれます。オニオンアーキテクチャやヘキサゴナルアーキテクチャは、DDDのテスト戦略に最適な基盤を提供します。

-   **依存関係逆転の原則**: すべての依存関係は、システムの中心にあるドメイン層に向かいます。ドメイン層は、UI、データベース、外部APIといった外部の関心事に一切依存しません。

-   **レイヤーごとのテスト責務**:
    -   **ドメイン層**: ビジネスロジックの核心。外部依存から完全に隔離されているため、高速な単体テストの主戦場となります。
    -   **アプリケーション層**: ユースケースの調整役。ドメインオブジェクトとインフラストラクチャのインターフェース（ポート）を連携させる流れを、統合テストで検証します。
    -   **インフラストラクチャ層**: 外部技術との接続。データベースとのマッピングや外部API呼び出しなどを、実際の技術（またはTestcontainersのようなツール）を使って統合テストで検証します。

## 3. テストピラミッド戦略: バランスの取れたアプローチ

DDDにおけるテスト戦略は、伝統的なテストピラミッドの考え方に従います。これは、システムの複雑なビジネスロジックが純粋なドメイン層に集約されているため、自然と形成される形です。

-   **レベル1: 単体テスト (Unit Tests) - 約70%**
    -   **対象**: ドメイン層（集約、値オブジェクト、ドメインサービス）。
    -   **特徴**: 最も数が多く、高速で安定。外部依存なしでメモリ上で完結します。

-   **レベル2: 統合テスト (Integration Tests) - 約20%**
    -   **対象**: アプリケーションサービス、リポジトリ、外部API連携。
    -   **特徴**: 複数のコンポーネント間の連携を検証します。

-   **レベル3: E2Eテスト (End-to-End Tests) - 約10%**
    -   **対象**: UIからデータベースまでの完全なユーザーストーリー。
    -   **特徴**: 主要な「ハッピーパス」シナリオに限定し、システム全体の健全性を確認します。

## 4. 戦術的設計: DDDビルディングブロックのテスト手法 (Spock編)

ここでは、各DDDコンポーネントに対する具体的なテスト戦略を、BDDスタイルのテストフレームワークであるSpockを用いて解説します。Spockの`given-when-then`ブロックは、DDDのテストを「仕様」として記述するのに非常に適しています。

### 4.1. 値オブジェクト (Value Object)

-   **焦点**: 不変性、等価性、検証ロジック。
-   **戦略**: Spockの`where`ブロックを使ったデータ駆動テストが特に有効です。

```groovy
// build.gradle.kts
// testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")

import spock.lang.Specification
import spock.lang.Unroll

class MoneySpec extends Specification {

    def "同じ金額と通貨を持つMoneyオブジェクトは等価である"() {
        given: "2つのMoneyオブジェクト"
        def moneyA = new Money(1000, "JPY")
        def moneyB = new Money(1000, "JPY")

        when: "等価性を比較すると"
        def result = (moneyA == moneyB)

        then: "trueが返される"
        result == true
        moneyA.hashCode() == moneyB.hashCode()
    }

    def "Moneyオブジェクトは不変である"() {
        given: "1000円のMoneyオブジェクト"
        def originalAmount = new Money(1000, "JPY")

        when: "500円を加算すると"
        def newAmount = originalAmount.add(new Money(500, "JPY"))

        then: "新しいインスタンスが返され、元は変更されない"
        newAmount.getAmount() == 1500
        originalAmount.getAmount() == 1000
    }

    @Unroll // テスト結果を個別に表示
    def "無効な金額(#amount)や通貨(#currency)では生成時に例外が発生する"() {
        when: "Moneyオブジェクトを生成しようとすると"
        new Money(amount, currency)

        then: "IllegalArgumentExceptionが発生する"
        thrown(IllegalArgumentException)

        where:
        amount | currency
        -100   | "JPY"
        1000   | null
        1000   | "jpy" // 小文字は許可しないルール
    }
}
```

### 4.2. エンティティと集約 (Entity & Aggregate)

-   **焦点**: トランザクション整合性、ビジネスルール、状態遷移、ドメインイベントの発行。
-   **戦略**: テストは常に関心のルートである**集約ルート**を通じて行います。1つのテストで1つのコマンド（操作）とその結果を検証します。

```groovy
import spock.lang.Specification

class LoanApplicationSpec extends Specification {

    def "ローン申請は、申込後に審査中に遷移する"() {
        given: "新規のローン申請集約"
        def applicantId = new ApplicantId(UUID.randomUUID())
        def loanApplication = LoanApplication.apply(applicantId, new Money(300, "JPY"))

        when: "申請を審査に出すと"
        loanApplication.submitForReview()

        then: "ステータスが'審査中'になる"
        loanApplication.getStatus() == LoanApplicationStatus.IN_REVIEW
    }

    def "承認済みの申請を、再度承認しようとすると例外が発生する"() {
        given: "承認済みのローン申請"
        def applicantId = new ApplicantId(UUID.randomUUID())
        def loanApplication = LoanApplication.apply(applicantId, new Money(500, "JPY"))
        loanApplication.submitForReview()
        loanApplication.approve(new ScoringResult(true, 80)) // 一度承認する

        when: "再度承認処理を実行すると"
        loanApplication.approve(new ScoringResult(true, 85))

        then: "IllegalStateExceptionが発生する"
        thrown(IllegalStateException)
    }

    def "申請が承認されると、ドメインイベントが発行される"() {
        given: "審査中のローン申請"
        def applicantId = new ApplicantId(UUID.randomUUID())
        def loanApplication = LoanApplication.apply(applicantId, new Money(200, "JPY"))
        loanApplication.submitForReview()

        when: "申請を承認すると"
        loanApplication.approve(new ScoringResult(true, 90))

        then: "発行されたドメインイベントは1つである"
        def events = loanApplication.pullDomainEvents()
        events.size() == 1
        events[0] instanceof LoanApplicationApprovedEvent
        
        and: "イベントの内容が正しい"
        def event = (LoanApplicationApprovedEvent) events[0]
        event.getLoanApplicationId() == loanApplication.getId()
    }
}
```

### 4.3. リポジトリ (Repository)

-   **焦点**: 永続化マッピングとクエリロジックの正しさ。
-   **戦略**: これは**統合テスト**です。ORMやDBドライバはモック化せず、**Testcontainers** を使って本番環境と同一のDB（例: PostgreSQL）に対してテストを実行します。

```groovy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import org.testcontainers.spock.Testcontainers

@SpringBootTest
@Transactional // 各テスト後にロールバック
@Testcontainers // TestcontainersをSpockで有効化
class LoanApplicationRepositorySpec extends Specification {

    // TestcontainersがPostgreSQLコンテナを起動・設定する
    // @Container
    // static PostgreSQLContainer postgreSQLContainer = ...

    @Autowired
    private LoanApplicationRepository loanApplicationRepository

    def "リポジトリは集約を永続化し、IDで正しく取得できる"() {
        given: "新しいローン申請集約"
        def applicantId = new ApplicantId(UUID.randomUUID())
        def original = LoanApplication.apply(applicantId, new Money(1000, "JPY"))

        when: "リポジトリに保存し、再度取得すると"
        loanApplicationRepository.save(original)
        def found = loanApplicationRepository.findById(original.getId()).get()

        then: "保存前と取得後のインスタンスは状態が等しい"
        found.getId() == original.getId()
        found.getStatus() == original.getStatus()
        found.getAmount() == original.getAmount()
    }
}
```

### 4.4. アプリケーションサービス (Application Service)

-   **焦点**: ユースケースのオーケストレーション（処理の調整役）。
-   **戦略**: 外部のインフラ層（リポジトリ、外部APIなど）は**テストダブル（モックやスタブ）**に置き換えます。これにより、アプリケーション層のロジックのみに集中できます。

```groovy
import spock.lang.Specification

class LoanApplicationServiceSpec extends Specification {

    private LoanApplicationService loanApplicationService
    private LoanApplicationRepository loanApplicationRepository // Mock
    private ScoringService scoringService // Mock

    void setup() {
        // SpockのMock()機能でモックを作成
        loanApplicationRepository = Mock()
        scoringService = Mock()
        loanApplicationService = new LoanApplicationService(loanApplicationRepository, scoringService)
    }

    def "申請を承認するユースケース"() {
        given: "審査中の申請がリポジトリに存在する"
        def loanId = new LoanApplicationId(UUID.randomUUID())
        def application = LoanApplication.apply(new ApplicantId(UUID.randomUUID()), new Money(1000, "JPY"))
        application.submitForReview()
        
        // リポジトリの振る舞いを定義
        loanApplicationRepository.findById(loanId) >> Optional.of(application)
        // スコアリングサービスの振る舞いを定義
        scoringService.execute(_) >> new ScoringResult(true, 80)

        when: "承認サービスを呼び出すと"
        loanApplicationService.approveApplication(loanId)

        then: "スコアリングサービスが1回呼び出される"
        1 * scoringService.execute(_)

        and: "リポジトリのsaveメソッドが1回呼び出される"
        1 * loanApplicationRepository.save({ LoanApplication app ->
            // 保存される集約の状態を検証
            app.getStatus() == LoanApplicationStatus.APPROVED
        })
    }
}
```

## 5. テストダブルの賢い使い方: Mock, Stub, Fake

-   **Stub**: テスト対象に「入力」を提供します（例: `repository.findById(...)`が特定のデータを返す）。
-   **Mock**: テスト対象からの「出力」を検証します（例: `emailService.send()`が呼び出されたか）。
-   **Fake**: 実際に動作する軽量な代替実装です（例: `HashMap`を使ったインメモリリポジトリ）。

**重要な指針: 自分が所有していない「管理外の依存関係」のみをモックする。**

-   **モックすべき対象 (OK)**: 外部の決済API、メール送信サービスなど。これらはシステムの境界であり、その「契約」が守られているかを検証します。
-   **モックすべきでない対象 (NG)**: 自身のデータベース（リポジトリ経由）。これは実装の詳細です。リポジトリをモックすると、テストがリファクタリングに非常に弱くなります。代わりに**インメモリのFake実装**を使うか、**TestcontainersによるDB統合テスト**を行いましょう。

## 6. まとめ

モダンなDDDのテスト戦略は、単なるバグ発見にとどまりません。それは設計品質を高め、ドキュメントとして機能し、チームに自信を与えるための中心的なプラクティスです。

-   **アーキテクチャがテスト容易性を決める**: 純粋なドメイン層が、高速で価値あるテストの基盤となる。
-   **テストピラミッドを意識する**: 各コンポーネントに適した種類のテストを、適切なバランスで配置する。
-   **BDDとSpockで仕様を記述する**: テストをビジネスの言葉で語らせる。
-   **テストダブルを賢く使う**: むやみにモックを使わず、システムの境界を意識する。

この戦略に従うことで、変更に強く、保守しやすく、そしてビジネス価値を正確に反映した、堅牢なシステムを構築することが可能になります。 