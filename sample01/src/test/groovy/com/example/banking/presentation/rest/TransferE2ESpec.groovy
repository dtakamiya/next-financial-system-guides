package com.example.banking.presentation.rest

import com.example.banking.domain.account.*
import com.example.banking.domain.transfer.Transfer
import com.example.banking.domain.transfer.TransferId
import com.example.banking.domain.transfer.TransferRepository
import org.awaitility.Awaitility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

/**
 * 振込機能のE2E(End-to-End)テスト。
 * このテストは、APIリクエストから非同期Saga処理、DBの状態変更まで、システム全体の振る舞いを検証します。
 *
 * @SpringBootTest Spring Bootアプリケーションを完全に起動してテストを実行します。
 * @Testcontainers Testcontainersライブラリと連携し、テスト用にDockerコンテナ（ここではPostgreSQL）を管理します。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransferE2ESpec extends Specification {

    /**
     * テスト用のPostgreSQLコンテナを定義。
     * @Shared により、このSpecクラス内の全テストメソッドで単一のコンテナインスタンスが共有されます。
     */
    @Shared
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withInitScript("schema.sql") // コンテナ起動時にテーブル作成スクリプトを実行

    // Spring Bootが起動したアプリケーションのDIコンテナからBeanをインジェクト
    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    AccountRepository accountRepository

    @Autowired
    TransferRepository transferRepository

    /**
     * Specクラスの全テストが実行される前に一度だけ実行されるセットアップメソッド。
     */
    void setupSpec() {
        postgres.start()
        // コンテナの動的な接続情報をSpring Bootのデータソース設定に上書き
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl())
        System.setProperty("spring.datasource.username", postgres.getUsername())
        System.setProperty("spring.datasource.password", postgres.getPassword())
    }

    void cleanupSpec() {
        postgres.stop()
    }

    def "正常な振込リクエストはSagaを経て完了し、口座残高が正しく更新される"() {
        given: "テストの前提条件として、2つの口座を準備"
        def sourceAccount = Account.open(accountRepository.nextAccountNumber(), new CustomerName("Source User"), Money.of("10000"))
        def destAccount = Account.open(accountRepository.nextAccountNumber(), new CustomerName("Dest User"), Money.of("5000"))
        accountRepository.save(sourceAccount)
        accountRepository.save(destAccount)

        and: "3000円の振込リクエストを作成"
        def request = new TransferRequest(
                sourceAccountId: sourceAccount.getId().value(),
                destinationAccountId: destAccount.getId().value(),
                amount: 3000
        )

        when: "実際に振込APIを呼び出す"
        def response = restTemplate.postForEntity("/api/transfers", request, Void.class)

        then: "APIは即座に 202 Accepted を返す"
        response.statusCode == HttpStatus.ACCEPTED
        def location = response.headers.getLocation()
        location != null

        and: "非同期処理の結果をポーリングして待つ (結果整合性の検証)"
        def transferId = UUID.fromString(location.path.split('/').last())
        // Awaitilityを使い、条件が満たされるまで最大10秒間、定期的にチェックを行う。
        // これにより、非同期であるSagaの完了を待ってからアサーションを実行できる。
        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> {
            def transfer = transferRepository.findById(new TransferId(transferId)).get()
            return transfer.getStatus() == Transfer.TransferStatus.COMPLETED
        })

        and: "最終的に両口座の残高が正しく更新されていることを確認"
        def updatedSource = accountRepository.findById(sourceAccount.getId()).get()
        def updatedDest = accountRepository.findById(destAccount.getId()).get()

        updatedSource.getBalance() == Money.of("7000")
        updatedDest.getBalance() == Money.of("8000")
    }

    def "残高不足の振込リクエストはSagaを経て失敗する"() {
        given: "残高が1000円しかない口座"
        def sourceAccount = Account.open(accountRepository.nextAccountNumber(), new CustomerName("Poor User"), Money.of("1000"))
        def destAccount = Account.open(accountRepository.nextAccountNumber(), new CustomerName("Rich User"), Money.of("50000"))
        accountRepository.save(sourceAccount)
        accountRepository.save(destAccount)

        and: "残高を超える3000円の振込リクエストを作成"
        def request = new TransferRequest(
                sourceAccountId: sourceAccount.getId().value(),
                destinationAccountId: destAccount.getId().value(),
                amount: 3000
        )

        when: "振込APIを呼び出す"
        def response = restTemplate.postForEntity("/api/transfers", request, Void.class)
        def location = response.headers.getLocation()
        def transferId = UUID.fromString(location.path.split('/').last())

        then: "Sagaが実行され、最終的にTransferの状態がFAILEDになることを待つ"
        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> {
            def transfer = transferRepository.findById(new TransferId(transferId)).get()
            return transfer.getStatus() == Transfer.TransferStatus.FAILED
        })

        and: "失敗したので、口座残高は変更されていないことを確認"
        def updatedSource = accountRepository.findById(sourceAccount.getId()).get()
        updatedSource.getBalance() == Money.of("1000")
    }
} 