# TODO: 次世代銀行システムサンプル (sample01) 開発計画

このドキュメントは、`guide01`から`guide06`までの設計思想と実践的パターンに基づき、ドメイン駆動設計（DDD）による銀行システムのサンプルアプリケーションを構築するためのタスクリストです。

## フェーズ1: プロジェクト基盤の構築 (Sprint 0)

-   [x] **1-1. Spring Bootプロジェクトの初期化**
    -   [ ] `start.spring.io`等を利用し、基本的なプロジェクトを作成する。
    -   [ ] **基本情報:**
        -   Group: `com.example.banking`
        -   Artifact: `sample01`
        -   Packaging: `Jar`
        -   Java: `17`
    -   [ ] **依存関係:**
        -   `Spring Web`: RESTful APIのため
        -   `Spring Boot DevTools`: 開発効率化
        -   `Lombok`: ボイラープレートコード削減
        -   `MyBatis Framework`: 永続化のため
        -   `PostgreSQL Driver`: 本番DB用
        -   `H2 Database`: ローカル/テスト用DB
        -   `Validation`: DTOの入力検証
-   [x] **1-2. テスト環境のセットアップ**
    -   [ ] **Spock**の依存関係を追加 (`spock-core`, `spock-spring`)。
    -   [ ] **Testcontainers**の依存関係を追加 (`testcontainers-spock`, `testcontainers-postgresql`)。
    -   [ ] `build.gradle.kts` または `pom.xml` を適切に設定する。
-   [x] **1-3. パッケージ構造の定義**
    -   [ ] クリーンアーキテクチャに基づき、主要パッケージを作成する。
        -   `com.example.banking.domain`: ドメイン層 (コアロジック)
        -   `com.example.banking.application`: アプリケーション層 (ユースケース)
        -   `com.example.banking.infrastructure`: インフラストラクチャ層 (DB, 外部API連携)
        -   `com.example.banking.presentation`: プレゼンテーション層 (REST Controller)
    -   [ ] ドメイン層の内部を、境界づけられたコンテキストでさらに分割する。
        -   `com.example.banking.domain.account`: 口座管理コンテキスト
        -   `com.example.banking.domain.transfer`: 振込コンテキスト

## フェーズ2: 口座管理コンテキストの実装

-   [x] **2-1. ドメインモデル (値オブジェクト) の実装とテスト**
    -   [ ] `AccountId` (UUIDベース)
    -   [ ] `Money` (金額と通貨, 不変, `BigDecimal`使用)
    -   [ ] `CustomerName`
    -   [ ] `AccountNumber`
    -   [ ] *Test(Spock)*: 各値オブジェクトの不変性、等価性、検証ロジックをテストする。
-   [x] **2-2. ドメインモデル (アグリゲート) の実装とテスト**
    -   [ ] `Account`アグリゲートを作成する。
        -   プロパティ: `AccountId`, `AccountNumber`, `CustomerName`, `Money` (残高)
        -   メソッド: `deposit(Money)`, `withdraw(Money)`
        -   ビジネスルール: 残高はマイナスになれない。
        -   イベント: `AccountOpened`, `Deposited`, `Withdrawn` ドメインイベントを発行する仕組みを実装。
    -   [ ] *Test(Spock)*: `Account`アグリゲートの各操作と、それに伴う状態遷移、イベント発行をテストする。
-   [x] **2-3. リポジトリ層の実装とテスト**
    -   [ ] `domain.account`に`AccountRepository`インターフェースを定義する (`findById`, `save`, `nextAccountNumber`)。
    -   [ ] `infrastructure.persistence`に`MyBatisAccountRepository`を実装する。
        -   `AccountMapper.xml` (SQL) と `AccountMapper.java` (Interface) を作成。
    -   [ ] `schema.sql`に`accounts`テーブル定義を追加する。楽観的ロック用の`version`カラムも忘れずに含める。
    -   [ ] *Test(Testcontainers-Spock)*: PostgreSQLコンテナを起動し、リポジトリの永続化・取得ロジックをテストする。楽観的ロックの競合テストも行う。
-   [x] **2-4. アプリケーションサービス層の実装とテスト**
    -   [ ] `application.service`に`AccountService`を作成する。
    -   [ ] `OpenAccountUseCase`を実装する (`openAccount`メソッド)。
    -   [ ] `DepositUseCase`を実装する (`deposit`メソッド)。
    -   [ ] `GetAccountQuery`を実装する (`getAccountDetails`メソッド)。
    -   [ ] *Test(Spock)*: リポジトリをモック化し、`AccountService`の各ユースケースが正しくドメインモデルとリポジトリを協調させているかテストする。
-   [x] **2-5. プレゼンテーション層の実装**
    -   [ ] `presentation.rest`に`AccountController`を作成する。
    -   [ ] 口座開設 (`POST /accounts`)、入金 (`POST /accounts/{accountId}/deposits`)、口座情報取得 (`GET /accounts/{accountId}`) のエンドポイントを実装する。
    -   [ ] DTO (`OpenAccountRequest`, `AccountDetailsResponse`等) を定義し、Bean Validationを適用する。

## フェーズ3: 振込コンテキストとSagaパターンの実装

-   [ ] **3-1. ドメインモデルの実装とテスト**
    -   [ ] `TransferId` (UUIDベース)
    -   [ ] `Transfer`アグリゲートを作成する。
        -   プロパティ: `TransferId`, `sourceAccountId`, `destinationAccountId`, `Money`, `status` (REQUESTED, COMPLETED, FAILED)
        -   イベント: `TransferRequested`
    -   [ ] *Test(Spock)*: `Transfer`アグリゲートのロジックとイベント発行をテストする。
-   [ ] **3-2. 振込ユースケースの実装**
    -   [ ] `domain.transfer`に`TransferRepository`インターフェースを定義し、インフラ層に実装する。
    -   [ ] `application.service`に`TransferService`を実装 (`requestTransfer`メソッド)。
        -   このサービスは`Transfer`アグリゲートを作成・保存し、`TransferRequested`イベントを発行する。
-   [ ] **3-3. Sagaのオーケストレーション (イベント駆動)**
    -   [ ] `application.saga`パッケージを作成する。
    -   [ ] `TransferSaga` (または`TransferEventHandler`) を作成する。
    -   [ ] `TransferRequested`イベントをリッスンする (`@EventListener`)。
    -   [ ] **Sagaのロジック:**
        1.  `TransferRequested`イベント受信。
        2.  `AccountService`を呼び出し、出金元口座から`withdraw`を実行。
        3.  成功した場合、`AccountService`を呼び出し、入金先口座へ`deposit`を実行。
        4.  すべて成功した場合、`Transfer`アグリゲートの状態を`COMPLETED`に更新。
        5.  途中で失敗した場合（例: 残高不足）、**補償トランザクション**を実行（出金を取り消す`deposit`処理など）、`Transfer`の状態を`FAILED`に更新。
-   [ ] **3-4. E2EテストによるSagaの検証**
    -   [ ] *Test(Testcontainers-Spock)*: Docker Composeを使い、アプリケーションとPostgreSQLを同時に起動するE2Eテストを作成する。
    -   [ ] 振込APIを呼び出し、非同期処理の結果、最終的に両口座の残高と`Transfer`アグリゲートの状態が期待通りになっていることを`Awaitility`などを使って検証する。
    -   [ ] 残高不足で失敗するシナリオのテストも作成する。

## フェーズ4: 仕上げとドキュメント

-   [ ] **4-1. APIドキュメントの整備**
    -   [ ] Springdoc (`springdoc-openapi-ui`) を導入し、Swagger UIを生成する。
-   [x] **4-2. コンフィグレーションの分離**
    -   [ ] `application.yml`を`dev`, `prod`プロファイルに分割する。
-   [x] **4-3. README.mdの作成**
    -   [ ] プロジェクトの概要、ビルド方法、実行方法、APIエンドポイント一覧を記載する。
-   [x] **4-4. コードクリーンアップと最終レビュー**
    -   [ ] 完了したコードクリーンアップと最終レビューを行う。 