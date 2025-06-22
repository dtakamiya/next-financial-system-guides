package com.example.banking.domain.account

import spock.lang.Specification
import spock.lang.Unroll

import java.math.BigDecimal

class MoneySpec extends Specification {

    def "正常な値でMoneyオブジェクトを生成できる"() {
        when: "正常な値でMoneyを生成すると"
        def money = new Money(new BigDecimal("1000"), Money.JPY)

        then: "オブジェクトが正しく生成される"
        money.amount() == new BigDecimal("1000")
        money.currency() == Money.JPY
    }

    @Unroll
    def "不正な引数 #amount, #currency でMoneyを生成すると例外が発生する"() {
        when: "不正な値でMoneyを生成しようとすると"
        new Money(amount, currency)

        then: "適切な例外がスローされる"
        thrown(expectedException)

        where:
        amount                 | currency  | expectedException
        null                   | Money.JPY | NullPointerException
        new BigDecimal("1000") | null      | NullPointerException
        new BigDecimal("-100") | Money.JPY | IllegalArgumentException
    }

    def "addメソッドは正しく金額を加算し、不変性を保つ"() {
        given: "1000円と500円のMoneyオブジェクト"
        def oneThousandYen = Money.of("1000")
        def fiveHundredYen = Money.of("500")

        when: "1000円に500円を加算すると"
        def result = oneThousandYen.add(fiveHundredYen)

        then: "結果は1500円になり、元のオブジェクトは変更されない"
        result.amount() == new BigDecimal("1500")
        oneThousandYen.amount() == new BigDecimal("1000") // Immutability check
    }

    def "subtractメソッドは正しく金額を減算し、不変性を保つ"() {
        given: "1000円と500円のMoneyオブジェクト"
        def oneThousandYen = Money.of("1000")
        def fiveHundredYen = Money.of("500")

        when: "1000円から500円を減算すると"
        def result = oneThousandYen.subtract(fiveHundredYen)

        then: "結果は500円になり、元のオブジェクトは変更されない"
        result.amount() == new BigDecimal("500")
        oneThousandYen.amount() == new BigDecimal("1000") // Immutability check
    }

    def "異なる通貨のMoneyオブジェクトを加算しようとすると例外が発生する"() {
        given: "円とドルのMoneyオブジェクト"
        def yen = Money.of("1000")
        def usd = new Money(new BigDecimal("10"), Currency.getInstance("USD"))

        when: "円とドルを加算しようとすると"
        yen.add(usd)

        then: "IllegalArgumentExceptionが発生する"
        thrown(IllegalArgumentException)
    }

    def "isGreaterThanメソッドは金額を正しく比較する"() {
        given: "1000円と500円のMoneyオブジェクト"
        def oneThousandYen = Money.of("1000")
        def fiveHundredYen = Money.of("500")

        expect: "比較結果が正しいこと"
        oneThousandYen.isGreaterThan(fiveHundredYen)
        !fiveHundredYen.isGreaterThan(oneThousandYen)
        !oneThousandYen.isGreaterThan(oneThousandYen)
    }
} 