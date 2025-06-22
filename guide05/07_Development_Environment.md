# Part 3: 実践編 - SpringBootによる実装

理論（Part 1）と設計（Part 2）の世界から、いよいよコードが主役となる実践の世界へようこそ。このPart 3では、これまでに設計した概念を、Java、Spring Boot、そしてモダンなテストフレームワークを使って、実際に動くソフトウェアとして形にしていきます。

---

# Chapter 7: 開発環境のセットアップ - 冒険の準備

優れた冒険には、信頼できる装備が必要です。この章では、ドメイン駆動の旅に出るための開発環境をセットアップし、プロジェクトの土台を固めます。目標は、誰が実行しても同じように動作する、再現性の高い開発環境を構築することです。

## 7.1. 必須となる道具（ツール）

我々の開発スタックは、堅牢かつモダンな選択をしています。

-   **Java Development Kit (JDK)**: **Java 17** を使用します。
    -   `SDKMAN!` を利用すると、バージョンの管理が非常に簡単になります。
        ```bash
        # SDKMAN! のインストール (未導入の場合)
        # https://sdkman.io/install
        
        # Java 17 (Temurinディストリビューション) のインストールと設定
        sdk install java 17.0.11-tem
        sdk default java 17.0.11-tem
        ```
-   **Gradle**: プロジェクトのビルドと依存関係の管理には、Mavenよりも柔軟で記述性の高い **Gradle** を採用します。
    -   プロジェクトに`gradlew` (Gradle Wrapper) が含まれるため、ローカルマシンにGradleを別途インストールする必要はありません。
-   **Docker Desktop**: データベース(PostgreSQL)やメッセージブローカー(Kafka)をローカルで手軽に動かすために必須です。
    -   [公式サイト](https://www.docker.com/products/docker-desktop/) からダウンロードし、インストールしておいてください。
-   **IDE (統合開発環境)**: **IntelliJ IDEA** (Community版で十分です)を強く推奨します。
    -   Spring Boot、Gradle、そして後述するSpock Frameworkとの連携が非常にスムーズです。

## 7.2. プロジェクトの骨格を作る (`build.gradle.kts`)

Gradleのビルドスクリプト `build.gradle.kts` は、プロジェクトの心臓部です。使用するライブラリ、プラグイン、プロジェクトの構造を定義します。

以下は、我々のプロジェクトの基盤となる設定です。このファイルをプロジェクトのルートに配置することから始めましょう。

```kotlin
// build.gradle.kts

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23" // Groovy(Spock)を動かすために必要
    groovy // Spock Frameworkのために必要
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // === Spring Boot Starter ===
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // === Database ===
    runtimeOnly("org.postgresql:postgresql")

    // === Testing ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // Spock Framework (Groovyベースの強力なテストフレームワーク)
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.spockframework:spock-spring:2.3-groovy-4.0")

    // Testcontainers (テストコードからDockerコンテナを操作)
    testImplementation("org.testcontainers:spock:1.19.7")
    testImplementation("org.testcontainers:postgresql:1.19.7")
    testImplementation("org.testcontainers:kafka:1.19.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### ディレクトリ構造

このビルド設定に従うと、プロジェクトのディレクトリ構造は以下のようになります。

```
.
├── build.gradle.kts          # プロジェクトの定義
├── gradlew                   # Gradle Wrapper (Unix/Mac)
├── gradlew.bat               # Gradle Wrapper (Windows)
└── src
    ├── main
    │   ├── java              # Javaソースコード (ドメイン、アプリ層など)
    │   └── resources         # 設定ファイル (application.ymlなど)
    └── test
        ├── groovy            # ★Spockテストコード
        └── resources         # テスト用設定ファイル
```
> **注意**: Spockのテストコードは `src/test/java` ではなく `src/test/groovy` に配置します。

## 7.3. ローカルインフラの構築 (`docker-compose.yml`)

開発中に毎回データベースやKafkaをクラウドにデプロイするのは非効率です。Docker Composeを使えば、コマンド一つでローカルに開発用のインフラ環境を構築できます。

プロジェクトのルートに以下の `docker-compose.yml` を作成します。

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:14
    container_name: ddd-local-postgres
    environment:
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=ddd_db
    ports:
      - "5432:5432"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    container_name: ddd-local-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    container_name: ddd-local-kafka
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

ターミナルで以下のコマンドを実行するだけで、開発に必要なミドルウェアが起動します。

```bash
# コンテナをバックグラウンドで起動
docker-compose up -d

# コンテナを停止・削除
docker-compose down
```

## 7.4. テストの実行

セットアップが正しく完了したかを確認する最も良い方法は、テストを実行することです。

-   **IDEから**: IntelliJ IDEAでは、テストファイルや各テストメソッドの横に表示される緑色の再生ボタンをクリックするだけで実行できます。
-   **コマンドラインから**: プロジェクトのルートディレクトリで以下のコマンドを実行します。

    ```bash
    # すべてのテストを実行
    ./gradlew test
    ```

---
これで冒険の準備は整いました。信頼できるツールと堅牢なインフラを手に入れた今、私たちは自信を持ってコーディングの海に漕ぎ出すことができます。

次の章では、この環境の上で、いよいよ最初のドメインモデルである「集約」を実装していきます。 