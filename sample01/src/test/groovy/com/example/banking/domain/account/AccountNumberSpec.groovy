package com.example.banking.domain.account

import spock.lang.Specification
import spock.lang.Unroll

class AccountNumberSpec extends Specification {

    def "正常な口座番号でAccountNumberを生成できる"() {
        when: "正常な口座番号で生成する"
        def accountNumber = new AccountNumber("1234567")

        then: "正しくオブジェクトが生成される"
        accountNumber.value() == "1234567"
    }

    @Unroll
    def "不正な口座番号 '#number' でAccountNumberを生成しようとすると #expectedException が発生する"() {
        when: "不正な口座番号で生成しようとすると"
        new AccountNumber(number)

        then: "指定された例外がスローされる"
        thrown(expectedException)

        where:
        number | expectedException
        null   | NullPointerException
        ""     | IllegalArgumentException
        "  "   | IllegalArgumentException
    }
} 