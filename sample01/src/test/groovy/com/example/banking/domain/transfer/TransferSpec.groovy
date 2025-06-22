package com.example.banking.domain.transfer

import com.example.banking.domain.account.AccountId
import com.example.banking.domain.account.Money
import spock.lang.Specification

class TransferSpec extends Specification {

    def "振込依頼が正しく生成される"() {
        given: "振込元、振込先、金額"
        def sourceAccountId = AccountId.newId()
        def destinationAccountId = AccountId.newId()
        def money = Money.of("3000")

        when: "振込を依頼すると"
        def transfer = Transfer.request(sourceAccountId, destinationAccountId, money)

        then: "TransferアグリゲートがREQUESTED状態で生成される"
        transfer.id != null
        transfer.sourceAccountId == sourceAccountId
        transfer.destinationAccountId == destinationAccountId
        transfer.money == money
        transfer.status == Transfer.TransferStatus.REQUESTED
        transfer.version == null
    }

    def "completeメソッドで状態がCOMPLETEDに遷移する"() {
        given: "REQUESTED状態の振込"
        def transfer = Transfer.request(AccountId.newId(), AccountId.newId(), Money.of("100"))

        when: "completeを呼び出す"
        transfer.complete()

        then: "状態がCOMPLETEDになる"
        transfer.status == Transfer.TransferStatus.COMPLETED
    }

    def "failメソッドで状態がFAILEDに遷移する"() {
        given: "REQUESTED状態の振込"
        def transfer = Transfer.request(AccountId.newId(), AccountId.newId(), Money.of("100"))

        when: "failを呼び出す"
        transfer.fail()

        then: "状態がFAILEDになる"
        transfer.status == Transfer.TransferStatus.FAILED
    }

    def "REQUESTED状態でないときにcompleteを呼び出すと例外が発生する"() {
        given: "COMPLETED状態の振込"
        def transfer = Transfer.request(AccountId.newId(), AccountId.newId(), Money.of("100"))
        transfer.complete()

        when: "再度completeを呼び出す"
        transfer.complete()

        then: "IllegalStateExceptionが発生する"
        thrown(IllegalStateException)
    }
} 