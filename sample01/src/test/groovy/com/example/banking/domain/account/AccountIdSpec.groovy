package com.example.banking.domain.account

import spock.lang.Specification

import java.util.UUID

class AccountIdSpec extends Specification {

    def "AccountIdはnullのUUIDで生成できない"() {
        when: "nullでAccountIdを生成しようとすると"
        new AccountId(null)

        then: "NullPointerExceptionが発生する"
        thrown(NullPointerException)
    }

    def "同じUUIDを持つAccountIdは等価である"() {
        given: "同じ値を持つUUID"
        def uuid = UUID.randomUUID()

        and: "そのUUIDから2つのAccountIdを生成"
        def id1 = new AccountId(uuid)
        def id2 = new AccountId(uuid)

        when: "2つのIDを比較すると"
        def areEqual = (id1 == id2)

        then: "trueが返される"
        areEqual
    }

    def "異なるUUIDを持つAccountIdは等価ではない"() {
        given: "2つの異なるAccountId"
        def id1 = AccountId.newId()
        def id2 = AccountId.newId()

        when: "2つのIDを比較すると"
        def areEqual = (id1 == id2)

        then: "falseが返される"
        !areEqual
    }

    def "newIdは常に新しいユニークなIDを生成する"() {
        when: "newIdを2回呼び出す"
        def id1 = AccountId.newId()
        def id2 = AccountId.newId()

        then: "生成されたIDは互いに異なる"
        id1 != id2
    }
} 