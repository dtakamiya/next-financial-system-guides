# 第7章：チュートリアル：振込マイクロサービスの構築

この章では、これまでの知識を総動員し、簡単な「振込API」を持つマイクロサービスをゼロから構築します。Testcontainersを使った信頼性の高いテストも記述し、モダンな開発フローを体験します。

## 🎯 チュートリアルのゴール
- `start.spring.io`からSpring Bootプロジェクトをセットアップできる。
- `Money`値オブジェクトと`Account`エンティティを実装できる。
- オニオンアーキテクチャに基づいたパッケージ構成で各レイヤーを実装できる。
- MyBatisとTestcontainers (PostgreSQL) を使って、信頼性の高いリポジトリの結合テストを作成できる。
- `curl`でAPIを実行し、DBのデータが変わることを確認できる。

---

## Step 1: プロジェクトのセットアップ
1.  [start.spring.io](https://start.spring.io/) にアクセスし、以下の依存関係を追加してプロジェクトを生成します。
    - **Dependencies**: `Spring Web`, `MyBatis Framework`, `PostgreSQL Driver`, `Spock Framework`, `Testcontainers`
2.  `pom.xml`にSpockのテスト実行に必要な`gmavenplus-plugin`を追加します（詳細は第6章参照）。

---

## Step 2: ドメイン層の実装
`com.example.transfer.domain.model`パッケージに、`Money`, `AccountId`, `Account`などを定義します。（詳細は第3章のコード例を参照）
特に`AccountRepository`インターフェースをドメイン層に定義することが重要です。

```java
// com.example.transfer.domain.model.account.AccountRepository.java
public interface AccountRepository {
    Optional<Account> findById(AccountId accountId);
    void save(Account account);
}
```

---

## Step 3: アプリケーション層の実装
`com.example.transfer.application`パッケージに、ユースケースを実装します。

**振込コマンドDTO**
```java
// dto/TransferCommand.java
public record TransferCommand(
    String fromAccountId, 
    String toAccountId, 
    BigDecimal amount, 
    String currency
) {}
```
**振込アプリケーションサービス**
```java
// service/TransferService.java
@Service
public class TransferService {
    private final AccountRepository accountRepository;

    // ... constructor

    @Transactional
    public void transferMoney(TransferCommand command) {
        // 1. 送金元と送金先の口座集約を取得
        var fromAccountId = new AccountId(command.fromAccountId());
        var fromAccount = accountRepository.findById(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException(fromAccountId));

        var toAccountId = new AccountId(command.toAccountId());
        var toAccount = accountRepository.findById(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException(toAccountId));

        // 2. ドメインロジックの実行
        var moneyToTransfer = new Money(command.amount(), command.currency());
        fromAccount.withdraw(moneyToTransfer);
        toAccount.deposit(moneyToTransfer);

        // 3. 変更された両方の口座集約を保存
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
```

---

## Step 4: インフラ層の実装
`com.example.transfer.infrastructure`パッケージに、DBとのやり取りを実装します。

1.  **`application.properties`**: Testcontainersから設定が提供されるため、DB関連の設定は空にしておきます。
    ```properties
    mybatis.mapper-locations=classpath:/mapper/*.xml
    ```
2.  **`MyBatisAccountRepository`**: `AccountRepository`インターフェースを実装します。楽観的ロックのチェックもここで行います。
3.  **`AccountMapper.java` / `AccountMapper.xml`**: SQLを記述します。`UPDATE`文では`version`カラムを必ず条件に含めます。

---

## Step 5: プレゼンテーション層の実装
`com.example.transfer.presentation`パッケージに、REST APIのエンドポイントを作成します。

```java
// controller/TransferController.java
@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferService transferService;
    // ... constructor

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferCommand command) {
        transferService.transferMoney(command);
        return ResponseEntity.ok().build();
    }
}
```

---

## Step 6: Testcontainersを使った結合テスト
`src/test/groovy`以下に、`MyBatisAccountRepository`の結合テストをSpockで記述します。

```groovy
// infrastructure.persistence.repository.MyBatisAccountRepositorySpec.groovy
package com.example.transfer.infrastructure.persistence.repository

import com.example.transfer.domain.model.account.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

@SpringBootTest
@Testcontainers
class MyBatisAccountRepositorySpec extends Specification {

    @Autowired
    AccountRepository accountRepository // テスト対象

    // PostgreSQLコンテナを定義
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withInitScript("schema.sql") // 初期化スクリプトを指定

    // コンテナの情報をSpringのデータソース設定に動的に反映
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }

    def "transfer between two accounts should update balances correctly"() {
        given: "two accounts with initial balances"
        def fromAccount = new Account(new AccountId("acc-from"), new CustomerId("cust-1"))
        fromAccount.deposit(new Money(new BigDecimal("1000"), "JPY"))
        accountRepository.save(fromAccount)

        def toAccount = new Account(new AccountId("acc-to"), new CustomerId("cust-2"))
        accountRepository.save(toAccount)
        
        when: "transferring 300 JPY from one to another"
        def service = new TransferService(accountRepository) // 実際はDIコンテナから取得
        def command = new TransferCommand("acc-from", "acc-to", new BigDecimal("300"), "JPY")
        service.transferMoney(command)

        then: "balances are updated correctly"
        def updatedFrom = accountRepository.findById(fromAccount.getAccountId()).get()
        def updatedTo = accountRepository.findById(toAccount.getAccountId()).get()

        updatedFrom.getBalance().amount() == new BigDecimal("700")
        updatedTo.getBalance().amount() == new BigDecimal("300")

        and: "versions are incremented"
        updatedFrom.getVersion() == 2L
        updatedTo.getVersion() == 2L
    }
}
```
*Note: `schema.sql`を`src/test/resources`に配置し、テーブル作成DDLを記述しておく必要があります。*

---

## Step 7: 動作確認
1.  **アプリケーションの起動**: `./mvnw spring-boot:run`
2.  **`curl`でテストデータの準備**: 口座を2つ作成する
    ```bash
    curl -X POST ... # Account 1作成
    curl -X POST ... # Account 2作成
    ```
3.  **`curl`で振込APIを実行**:
    ```bash
    curl -i -X POST -H "Content-Type: application/json" \
    -d '{"fromAccountId": "...", "toAccountId": "...", "amount": 500, "currency": "JPY"}' \
    http://localhost:8080/transfers
    ```
    `HTTP 200 OK`が返ることを確認します。その後、DBを直接確認するか、GET APIを作成して残高が更新されていることを確認します。

---

お疲れ様でした！これで、DDDとモダンなテスト手法に基づいたマイクロサービス開発の第一歩を踏み出すことができました。 