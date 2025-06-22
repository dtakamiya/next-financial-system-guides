package com.example.banking.infrastructure.persistence

import com.example.banking.domain.account.Account
import com.example.banking.domain.account.AccountNumber
import com.example.banking.domain.account.CustomerName
import com.example.banking.domain.account.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@Testcontainers
@Transactional
class MyBatisAccountRepositorySpec extends Specification {

    @Shared
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withInitScript("schema.sql")

    @Autowired
    MyBatisAccountRepository repository

    void setupSpec() {
        postgres.start()
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl())
        System.setProperty("spring.datasource.username", postgres.getUsername())
        System.setProperty("spring.datasource.password", postgres.getPassword())
    }

    void cleanupSpec() {
        postgres.stop()
    }

    def "新規口座を保存し、IDで取得できる"() {
        given: "新規に開設した口座"
        def accountNumber = repository.nextAccountNumber()
        def account = Account.open(accountNumber, new CustomerName("Taro Yamada"), Money.of("50000"))

        when: "リポジトリに保存する"
        repository.save(account)

        and: "保存したIDで再度取得する"
        def foundAccount = repository.findById(account.getId()).orElse(null)

        then: "正しく口座情報が取得できる"
        foundAccount != null
        foundAccount.getId() == account.getId()
        foundAccount.getAccountNumber().value() == accountNumber.value()
        foundAccount.getBalance() == Money.of("50000")
        foundAccount.getVersion() == 0L // 初期バージョンは0
    }

    def "既存口座の情報を更新できる"() {
        given: "永続化された口座"
        def accountNumber = repository.nextAccountNumber()
        def account = Account.open(accountNumber, new CustomerName("Jiro Suzuki"), Money.of("10000"))
        repository.save(account)
        def savedAccount = repository.findById(account.getId()).get()

        when: "口座の残高を変更して保存する"
        savedAccount.deposit(Money.of("5000"))
        repository.save(savedAccount)

        and: "再度口座を取得する"
        def updatedAccount = repository.findById(account.getId()).get()

        then: "残高が更新され、バージョンがインクリメントされている"
        updatedAccount.getBalance() == Money.of("15000")
        updatedAccount.getVersion() == 1L
    }

    def "同じバージョンで2回更新しようとすると楽観的ロックエラーが発生する"() {
        given: "永続化された口座"
        def accountNumber = repository.nextAccountNumber()
        def account = Account.open(accountNumber, new CustomerName("Saburo Tanaka"), Money.of("20000"))
        repository.save(account)

        and: "同じ口座を2つのインスタンスとして取得する"
        def account1 = repository.findById(account.getId()).get()
        def account2 = repository.findById(account.getId()).get()

        when: "最初のインスタンスで更新"
        account1.deposit(Money.of("1000"))
        repository.save(account1)

        and: "次に、古いバージョンのままの2番目のインスタンスで更新しようとすると"
        account2.withdraw(Money.of("2000"))
        repository.save(account2)

        then: "IllegalStateException（楽観的ロック失敗）が発生する"
        thrown(IllegalStateException)
    }
} 