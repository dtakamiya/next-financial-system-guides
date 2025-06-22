# 9. ハンズオンチュートリアル: 0から始める口座開設API開発

この章では、これまでの章で学んだ概念を実践するために、簡単な「口座開設API」をゼロから構築します。このチュートリアルを通して、DDDのレイヤー化されたアーキテクチャが、どのようにコードとして形作られていくのかを体験しましょう。

## 🎯 このチュートリアルのゴール
- Spring Bootプロジェクトのセットアップができる
- ドメインモデル（エンティティ、値オブジェクト）を`record`で実装できる
- DDDの各レイヤー（ドメイン、アプリケーション、インフラ、プレゼンテーション）の責務をコードレベルで理解できる
- MyBatisを使ってドメインオブジェクトの永続化ができる
- Spock Frameworkでテストを作成できる
- `curl`を使ってAPIの動作確認ができる

---

## Step 1: プロジェクトのセットアップ

まず、Spring Initializr を使ってプロジェクトの骨格を作成します。

1.  [start.spring.io](https://start.spring.io/) にアクセスします。
2.  以下の通りにプロジェクトを設定します。
    *   **Project**: `Maven`
    *   **Language**: `Java`
    *   **Spring Boot**: `3.2.x` (またはガイドで指定されているバージョン)
    *   **Project Metadata**:
        *   Group: `com.example`
        *   Artifact: `account-service`
        *   Name: `account-service`
        *   Description: `Demo project for Spring Boot`
        *   Package name: `com.example.accountservice`
    *   **Packaging**: `Jar`
    *   **Java**: `17`
3.  **Dependencies**（依存関係）で、以下のライブラリを追加します。
    *   `Spring Web`
    *   `MyBatis Framework`
    *   `H2 Database` (チュートリアル用のインメモリDB)
    *   `Lombok` (一部ボイラープレートコード削減のため)
    *   `Validation`
4.  **GENERATE** ボタンをクリックし、`account-service.zip` をダウンロードします。
5.  ダウンロードしたzipファイルを解凍し、お好みのIDE（例: IntelliJ IDEA, VSCode）で開きます。

これで、開発の準備が整いました。

---

## Step 2: ドメインのモデリング (Domain Layer)

最初に、ビジネスの中心となるドメインモデルを定義します。今回は「口座(Account)」と「金額(Money)」です。これらは `src/main/java/com/example/accountservice/domain/model` パッケージに作成します。

#### 1. `Money` 値オブジェクトの作成

まず、金額と通貨を表現する `Money` 値オブジェクトを作成します。`BigDecimal` を直接扱うのではなく、専用の型を用意することで、意図しない計算ミスを防ぎ、金額に関するビジネスルール（例: マイナス金額の禁止）をカプセル化できます。

```java
// src/main/java/com/example/accountservice/domain/model/Money.java
package com.example.accountservice.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public static final Money ZERO_JPY = new Money(BigDecimal.ZERO, "JPY");

    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### 2. `Account` エンティティの作成

次に、口座情報を保持する `Account` エンティティを作成します。エンティティは一意なID（`accountId`）を持ち、状態（`balance`）を変更するビジネスロジック（`deposit`, `withdraw`）を持ちます。

*注意: 簡潔にするため、このクラスではLombokのようなアノテーションは使わず、明示的にコンストラクタやゲッターを記述します。*

```java
// src/main/java/com/example/accountservice/domain/model/Account.java
package com.example.accountservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Account {
    private final String accountId;
    private final String customerId;
    private Money balance;
    private long version;

    // 新規作成時のコンストラクタ
    public Account(String customerId) {
        this.accountId = UUID.randomUUID().toString();
        this.customerId = Objects.requireNonNull(customerId);
        this.balance = Money.ZERO_JPY;
        this.version = 1L;
    }

    // 永続化層からの再構築用コンストラクタ
    public Account(String accountId, String customerId, Money balance, long version) {
        this.accountId = Objects.requireNonNull(accountId);
        this.customerId = Objects.requireNonNull(customerId);
        this.balance = Objects.requireNonNull(balance);
        this.version = version;
    }

    // 入金
    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    // ゲッター
    public String getAccountId() { return accountId; }
    public String getCustomerId() { return customerId; }
    public Money getBalance() { return balance; }
    public long getVersion() { return version; }
}
```
*Note: `withdraw`（出金）メソッドは、より複雑なルール（残高チェックなど）が必要になるため、このチュートリアルでは簡単にするため `deposit`（入金）のみを実装します。*

---

## Step 3: 永続化のインターフェース (Domain Layer)

ドメインモデルが、どのようにデータベースに保存されるかを意識しないように、永続化処理を抽象化するインターフェースを定義します。これを **リポジトリパターン** と呼びます。

このインターフェースはドメイン層に属します。
`src/main/java/com/example/accountservice/domain/repository` パッケージを作成し、その中に `AccountRepository.java` を作成します。

```java
// src/main/java/com/example/accountservice/domain/repository/AccountRepository.java
package com.example.accountservice.domain.repository;

import com.example.accountservice.domain.model.Account;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(String accountId);
    void save(Account account);
    boolean existsById(String accountId);
}
```
この時点では、MyBatisなどの具体的な技術に関する記述は一切ありません。ドメイン層は永続化の「方法」を知らない、という点が重要です（依存性逆転の原則）。 

---

## Step 4: ユースケースの実装 (Application Layer)

アプリケーション層は、ドメインモデルを使って具体的なユースケース（＝システムの機能）を実現します。今回は「顧客IDを受け取って、新しい口座を開設する」というユースケースです。

`src/main/java/com/example/accountservice/application` パッケージ以下に作成します。

#### 1. コマンドオブジェクトの作成
まず、リクエストの入力データを保持するためのDTO（Data Transfer Object）を作成します。これはコマンド（「〜せよ」という命令）の役割を持つため、`OpenAccountCommand`と名付けます。

```java
// src/main/java/com/example/accountservice/application/dto/OpenAccountCommand.java
package com.example.accountservice.application.dto;

import jakarta.validation.constraints.NotBlank;

// record を使うと、不変でフィールドを持つだけのクラスを簡潔に定義できる
public record OpenAccountCommand(@NotBlank String customerId) {}
```
*Note: `jakarta.validation.constraints.NotBlank` アノテーションを付与することで、このフィールドが空やnullであってはいけない、という制約を後のステップで自動的にチェックできます。*

#### 2. アプリケーションサービスの作成
次に、ユースケースの処理本体を実装する `AccountApplicationService` を作成します。

-   `AccountRepository` を使ってドメインオブジェクトを永続化層とやり取りします。
-   `@Transactional` アノテーションを付与することで、メソッド全体の処理が単一のトランザクションとして実行されることを保証します。

```java
// src/main/java/com/example/accountservice/application/service/AccountApplicationService.java
package com.example.accountservice.application.service;

import com.example.accountservice.application.dto.OpenAccountCommand;
import com.example.accountservice.domain.model.Account;
import com.example.accountservice.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountApplicationService {
    private final AccountRepository accountRepository;

    public AccountApplicationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public String openAccount(OpenAccountCommand command) {
        // 1. ドメインオブジェクトを生成
        Account newAccount = new Account(command.customerId());

        // 2. リポジトリを使って永続化
        accountRepository.save(newAccount);

        // 3. 生成された口座IDを返す
        return newAccount.getAccountId();
    }
}
```

---

## Step 5: 永続化の実装 (Infrastructure Layer)

インフラストラクチャ層では、ドメイン層で定義されたインターフェース（`AccountRepository`）を、MyBatisやH2 Databaseといった具体的な技術を使って実装します。

`src/main/java/com/example/accountservice/infrastructure` パッケージ以下に作成します。

#### 1. H2データベースの設定とテーブル定義
チュートリアルを簡単にするため、インメモリデータベースのH2を使用します。アプリケーション起動時に自動的にテーブルが作成されるように設定します。

**`src/main/resources/application.properties`**
```properties
# H2 Database Settings
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# MyBatis Settings
mybatis.mapper-locations=classpath:/mapper/*.xml
```

**`src/main/resources/schema.sql`**
アプリケーション起動時に実行されるSQLファイルを作成し、テーブルを定義します。
```sql
CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    balance_amount DECIMAL(19, 4) NOT NULL,
    balance_currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL
);
```

#### 2. MyBatis Mapper インターフェースの作成
MyBatisがSQLを実行するためのMapperインターフェースを定義します。

```java
// src/main/java/com/example/accountservice/infrastructure/persistence/mapper/AccountMapper.java
package com.example.accountservice.infrastructure.persistence.mapper;

import com.example.accountservice.domain.model.Account;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface AccountMapper {
    Optional<Account> findById(String accountId);
    int insert(Account account);
    int update(Account account);
    boolean existsById(String accountId);
}
```

#### 3. リポジトリの実装
ドメイン層の `AccountRepository` インターフェースの具体的な実装クラスを作成します。このクラスが `AccountMapper` を使ってデータベースとやり取りします。

```java
// src/main/java/com/example/accountservice/infrastructure/persistence/repository/MyBatisAccountRepository.java
package com.example.accountservice.infrastructure.persistence.repository;

import com.example.accountservice.domain.model.Account;
import com.example.accountservice.domain.repository.AccountRepository;
import com.example.accountservice.infrastructure.persistence.mapper.AccountMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisAccountRepository implements AccountRepository {
    private final AccountMapper accountMapper;

    public MyBatisAccountRepository(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return accountMapper.findById(accountId);
    }

    @Override
    public boolean existsById(String accountId) {
        return accountMapper.existsById(accountId);
    }

    @Override
    public void save(Account account) {
        // existsById を使って新規か更新かを判断
        if (!existsById(account.getAccountId())) {
            accountMapper.insert(account);
        } else {
            int updatedCount = accountMapper.update(account);
            // 楽観的ロック: 更新件数が0件の場合は、他の誰かが先にデータを更新したことを意味する
            if (updatedCount == 0) {
                throw new OptimisticLockingFailureException(
                    "Failed to update account " + account.getAccountId() + ". Version mismatch.");
            }
        }
    }
}
```

#### 4. Mapper XML の作成
SQLクエリをXMLファイルに記述します。`resultMap` を使って、データベースのカラムとドメインオブジェクトのフィールドをマッピングするのがポイントです。特に `Money` 値オブジェクトのようにネストしたオブジェクトも、`constructor` を使って直接マッピングできます。

`src/main/resources/mapper/AccountMapper.xml` を作成します。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.accountservice.infrastructure.persistence.mapper.AccountMapper">

    <resultMap id="accountResultMap" type="com.example.accountservice.domain.model.Account">
        <constructor>
            <idArg column="account_id" javaType="java.lang.String"/>
            <arg column="customer_id" javaType="java.lang.String"/>
            <arg javaType="com.example.accountservice.domain.model.Money" resultMap="moneyResultMap"/>
            <arg column="version" javaType="long"/>
        </constructor>
    </resultMap>

    <resultMap id="moneyResultMap" type="com.example.accountservice.domain.model.Money">
        <constructor>
            <arg column="balance_amount" javaType="java.math.BigDecimal"/>
            <arg column="balance_currency" javaType="java.lang.String"/>
        </constructor>
    </resultMap>

    <select id="findById" resultMap="accountResultMap">
        SELECT 
            account_id, 
            customer_id, 
            balance_amount, 
            balance_currency,
            version
        FROM accounts WHERE account_id = #{accountId}
    </select>

    <select id="existsById" resultType="boolean">
        SELECT COUNT(*) > 0 FROM accounts WHERE account_id = #{accountId}
    </select>
    
    <insert id="insert" parameterType="com.example.accountservice.domain.model.Account">
        INSERT INTO accounts (account_id, customer_id, balance_amount, balance_currency, version)
        VALUES (#{accountId}, #{customerId}, #{balance.amount}, #{balance.currency}, #{version})
    </insert>
    
    <update id="update" parameterType="com.example.accountservice.domain.model.Account">
        UPDATE accounts SET 
            balance_amount = #{balance.amount},
            balance_currency = #{balance.currency},
            version = version + 1
        WHERE account_id = #{accountId} AND version = #{version}
    </update>

</mapper>
```

---

## Step 6: APIエンドポイントの作成 (Presentation Layer)

最後に、外部（例: フロントエンドや他のマイクロサービス）からのHTTPリクエストを受け付ける口となるAPIコントローラーを作成します。

`src/main/java/com/example/accountservice/presentation/controller` パッケージ以下に作成します。

```java
// src/main/java/com/example/accountservice/presentation/controller/AccountController.java
package com.example.accountservice.presentation.controller;

import com.example.accountservice.application.dto.OpenAccountCommand;
import com.example.accountservice.application.service.AccountApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountApplicationService accountService;

    public AccountController(AccountApplicationService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Void> openAccount(@Valid @RequestBody OpenAccountCommand command) {
        // 1. アプリケーションサービスを呼び出す
        String accountId = accountService.openAccount(command);

        // 2. 作成されたリソースのURIを生成
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(accountId)
                .toUri();
        
        // 3. HTTP 201 Created を返す
        return ResponseEntity.created(location).build();
    }
}
```
*Note: `@Valid` アノテーションを付けることで、`OpenAccountCommand` の `@NotBlank` 制約がバリデーションされ、違反している場合は自動的にHTTP 400 Bad Requestが返されます。* 

---

## Step 7: テスト (Spock Framework)

アプリケーションが期待通りに動作することを保証するために、テストを作成します。ここでは、BDD (ビヘイビア駆動開発) スタイルのテストを記述できる Spock Framework を使用します。

#### 1. Spock/Groovy の依存関係とビルド設定の追加
SpockはGroovyベースのフレームワークなので、`pom.xml` にいくつかの設定を追加する必要があります。

**a. Groovyの依存関係を追加** (`<dependencies>`タグ内)
```xml
<dependency>
    <groupId>org.spockframework</groupId>
    <artifactId>spock-core</artifactId>
    <version>2.3-groovy-4.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.spockframework</groupId>
    <artifactId>spock-spring</artifactId>
    <version>2.3-groovy-4.0</version>
    <scope>test</scope>
</dependency>
```

**b. ビルドプラグインの追加** (`<build><plugins>`タグ内)
```xml
<plugin>
    <groupId>org.codehaus.gmavenplus</groupId>
    <artifactId>gmavenplus-plugin</artifactId>
    <version>1.13.1</version>
    <executions>
        <execution>
            <goals>
                <goal>addTestSources</goal>
                <goal>compileTests</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
*Note: `pom.xml` を変更した後は、IDEのMavenプロジェクト再読み込み機能を使って変更を適用してください。*

#### 2. ドメインモデルの単体テスト (Unit Test)
ドメインモデルのビジネスロジックが正しく動作することを確認します。テストコードは `src/test/groovy` 以下に作成します。

```groovy
// src/test/groovy/com/example/accountservice/domain/model/AccountSpec.groovy
package com.example.accountservice.domain.model

import spock.lang.Specification

import java.math.BigDecimal

class AccountSpec extends Specification {

    def "a new account should have a zero balance and a version of 1"() {
        given:
        def customerId = "cust-123"

        when:
        def account = new Account(customerId)

        then:
        account.getCustomerId() == customerId
        account.getBalance() == Money.ZERO_JPY
        account.getVersion() == 1L
        account.getAccountId() != null
    }

    def "depositing money should increase the balance"() {
        given:
        def account = new Account("cust-123")
        def depositAmount = new Money(new BigDecimal("1000"), "JPY")

        when:
        account.deposit(depositAmount)

        then:
        account.getBalance().amount() == new BigDecimal("1000")
        account.getBalance().currency() == "JPY"
    }
}
```

#### 3. APIの結合テスト (Integration Test)
`@SpringBootTest` を使い、実際にアプリケーションを起動した状態でAPIエンドポイントをテストします。

```groovy
// src/test/groovy/com/example/accountservice/presentation/controller/AccountControllerSpec.groovy
package com.example.accountservice.presentation.controller

import com.example.accountservice.application.dto.OpenAccountCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate

    def "POST /accounts should create a new account and return 201 Created"() {
        given: "a valid request to open an account"
        def command = new OpenAccountCommand("cust-456")

        when: "the endpoint is called"
        def response = restTemplate.postForEntity("/accounts", command, Void.class)

        then: "the response should be 201 Created"
        response.statusCode == HttpStatus.CREATED

        and: "the Location header should be present"
        response.headers.getLocation() != null
        println "Created resource at: " + response.headers.getLocation()
    }

    def "POST /accounts with a blank customerId should return 400 Bad Request"() {
        given: "an invalid request with a blank customerId"
        def command = new OpenAccountCommand("") // Invalid

        when: "the endpoint is called"
        def response = restTemplate.postForEntity("/accounts", command, Void.class)

        then: "the response should be 400 Bad Request"
        response.statusCode == HttpStatus.BAD_REQUEST
    }
}
```
IDEからこれらのテストを実行し、すべてグリーン（成功）になることを確認してください。

---

## Step 8: 動作確認

最後に、実際にアプリケーションを起動して、`curl` コマンドでAPIを叩いてみましょう。

#### 1. アプリケーションの起動
ターミナルを開き、プロジェクトのルートディレクトリで以下のコマンドを実行します。
```bash
./mvnw spring-boot:run
```
コンソールに `Tomcat started on port(s): 8080 (http)` のようなログが表示されれば起動成功です。

#### 2. `curl` でAPIを呼び出す
別のターミナルを開き、以下の `curl` コマンドを実行して口座開設APIを呼び出します。
```bash
curl -i -X POST -H "Content-Type: application/json" -d '{"customerId": "user-777"}' http://localhost:8080/accounts
```

**期待される結果**
以下のような `HTTP 201 Created` のレスポンスが返ってくるはずです。`Location` ヘッダーには、作成された口座リソースのURLが含まれます。
```
HTTP/1.1 201 Created
Location: http://localhost:8080/accounts/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
Content-Length: 0
Date: ...
```

#### 3. H2コンソールでデータを確認する
ブラウザで `http://localhost:8080/h2-console` を開きます。
以下の情報で接続します。
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password**: `password`

**Connect** ボタンを押し、`SELECT * FROM ACCOUNTS;` を実行すると、`curl`で作成したデータがデータベースに保存されていることを確認できます。

---

お疲れ様でした！これで、DDDのレイヤードアーキテクチャに基づいた簡単なマイクロサービスの開発フローを一通り体験できました。このチュートリアルが、日々の開発の助けとなれば幸いです。 