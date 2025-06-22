# Chapter 2: 開発環境と共通ライブラリの準備

前章で描いた設計図をコードに落とし込むため、まずは開発の土台となる環境と、プロジェクト全体で再利用する共通コンポーネントを準備します。この章では、一貫性のある効率的な開発フローを確立するための基盤を構築します。

## 2.1. 開発環境のセットアップ

本ガイドでは、以下の技術スタックを前提とします。事前に各ツールのインストールと設定を済ませておいてください。

-   **Java**: `17` or later
-   **Maven/Gradle**: プロジェクトのビルドと依存関係管理
-   **Docker**: ミドルウェア（データベース、メッセージブローカー等）のコンテナ化
-   **IDE**: IntelliJ IDEA, VSCodeなど（お好みのもの）

### ミドルウェアのコンテナ化 (`docker-compose.yml`)

マイクロサービスアーキテクチャでは、データベースやメッセージブローカーなど、複数のミドルウェアが必要になります。これらの環境をローカルマシンで手軽に再現するため、Docker Composeを利用します。

プロジェクトのルートディレクトリに、以下の`docker-compose.yml`を作成します。

```yaml
# docker-compose.yml
version: '3.8'

services:
  # PostgreSQL: 各マイクロサービスが使用するデータベース
  postgres:
    image: postgres:14
    container_name: ddd-postgres
    environment:
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=ddd_db
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  # Kafka: サービス間の非同期メッセージング用ブローカー
  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    container_name: ddd-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    container_name: ddd-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  postgres-data:
```

このファイルにより、以下のコマンド一つで必要なミドルウェア群を起動・停止できます。

```bash
# 起動
docker-compose up -d

# 停止
docker-compose down
```

---

## 2.2. マルチプロジェクト構成

本プロジェクトでは、複数のマイクロサービスを1つのGitリポジトリで管理する「モノレポ」構成を採用します。これにより、サービス間の依存関係の管理や、共通ライブラリの更新が容易になります。

プロジェクトのルートには親の`pom.xml`（Mavenの場合）または`settings.gradle`（Gradleの場合）を配置し、各マイクロサービスや共通ライブラリをサブモジュールとして定義します。

**ディレクトリ構成例 (Maven):**
```
.
├── pom.xml                   # 親POM
├── docker-compose.yml
├── common-ddd/               # 共通DDDライブラリ
│   └── pom.xml
├── application-service/      # 申請受付サービス
│   └── pom.xml
├── scoring-service/          # 信用評価サービス
│   └── pom.xml
...
```

---

## 2.3. 共通ライブラリの作成

マイクロサービス間で共通するロジックや設定は、専用のライブラリ（モジュール）として切り出すことで、コードの重複を防ぎ、一貫性を保つことができます。

### `common-ddd` モジュール

ドメイン駆動設計の戦術的パターンに関連する、汎用的なコンポーネントを配置します。

-   **`AggregateRoot.java`**: 集約のルートとなるエンティティの基底クラス。ドメインイベントを保持・公開する責務を持つ。
-   **`DomainEvent.java`**: すべてのドメインイベントが実装するマーカーインターフェース。
-   **`DomainEventPublisher.java`**: ドメインイベントを発行するためのインターフェース。実装は各サービスのインフラ層が担当する。

### `common-web` モジュール

REST APIを提供するサービスで共通となる、Web層の関心事を扱います。

-   **`GlobalExceptionHandler.java`**: `DomainException`や`NotFoundException`など、アプリケーション全体で発生する例外を補足し、統一されたエラーレスポンスを生成する。
-   **`ApiResponse.java`**: APIの成功・失敗時のレスポンス形式を標準化するためのラッパークラス。

これらの共通ライブラリを各マイクロサービスが依存関係として取り込むことで、開発者はビジネスロジックの実装に集中できます。

---

これで、本格的な実装に入るための準備が整いました。次の章から、最初のマイクロサービスである「申請受付サービス」の実装を開始します。 