package com.example.banking.domain.account

import spock.lang.Specification

class AccountSpec extends Specification {

    def "口座開設が正しく行われる"() {
        given: "顧客名と初期預金額"
        def accountNumber = new AccountNumber("12345")
        def customerName = new CustomerName("Taro Suzuki")
        def initialDeposit = Money.of("10000")

        when: "口座を開設すると"
        def account = Account.open(accountNumber, customerName, initialDeposit)

        then: "口座が正しく初期化される"
        account.id != null
        account.accountNumber == accountNumber
        account.customerName == customerName
        account.balance == initialDeposit
    }

    def "正の金額を入金すると残高が増加する"() {
        given: "残高10000円の口座"
        def account = Account.open(new AccountNumber("54321"), new CustomerName("Jiro Tanaka"), Money.of("10000"))

        when: "5000円を入金すると"
        account.deposit(Money.of("5000"))

        then: "残高は15000円になる"
        account.balance == Money.of("15000")
    }

    def "ゼロを入金しようとすると例外が発生する"() {
        given: "任意の口座"
        def account = Account.open(new AccountNumber("54321"), new CustomerName("Jiro Tanaka"), Money.of("10000"))

        when: "0円を入金しようとすると"
        account.deposit(Money.zero())

        then: "IllegalArgumentExceptionが発生する"
        thrown(IllegalArgumentException)
    }

    def "残高の範囲内で出金すると残高が減少する"() {
        given: "残高10000円の口座"
        def account = Account.open(new AccountNumber("98765"), new CustomerName("Hanako Sato"), Money.of("10000"))

        when: "3000円を出金すると"
        account.withdraw(Money.of("3000"))

        then: "残高は7000円になる"
        account.balance == Money.of("7000")
    }

    def "残高を超える金額を出金しようとすると例外が発生する"() {
        given: "残高10000円の口座"
        def account = Account.open(new AccountNumber("98765"), new CustomerName("Hanako Sato"), Money.of("10000"))

        when: "15000円を出金しようとすると"
        account.withdraw(Money.of("15000"))

        then: "IllegalStateExceptionが発生する"
        thrown(IllegalStateException)
    }
} 