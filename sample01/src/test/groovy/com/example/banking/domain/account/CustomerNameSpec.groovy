package com.example.banking.domain.account

import spock.lang.Specification
import spock.lang.Unroll

class CustomerNameSpec extends Specification {

    def "正常な名前でCustomerNameを生成できる"() {
        when: "正常な名前で生成する"
        def name = new CustomerName("Taro Yamada")

        then: "正しくオブジェクトが生成される"
        name.value() == "Taro Yamada"
    }

    @Unroll
    def "不正な名前 '#name' でCustomerNameを生成しようとすると #expectedException が発生する"() {
        when: "不正な名前で生成しようとすると"
        new CustomerName(name)

        then: "指定された例外がスローされる"
        thrown(expectedException)

        where:
        name  | expectedException
        null  | NullPointerException
        ""    | IllegalArgumentException
        "  "  | IllegalArgumentException
    }
} 