# Chapter 10: アプリケーションサービスとドメインサービス - 責務の明確な分離

私たちはこれまでに、ドメインの核となる「集約」と、それを永続化する「リポジトリ」を実装しました。しかし、実際のユースケース（例：「ユーザーがローンを申請する」）を実行するには、これらの部品を組み合わせて調整する役割が必要です。その役割を担うのが「サービス」と呼ばれるコンポーネントです。

DDDでは、サービスは主に2種類に分類されます。それぞれの役割を正しく理解し、使い分けることが、クリーンで保守性の高いコードを書くための鍵となります。

## 10.1. アプリケーションサービス - ユースケースの指揮者

**アプリケーションサービス (Application Service)** は、システムの**ユースケース**を直接的に表現します。その主な役割は、ドメインオブジェクトとインフラストラクチャを調整し、一つの業務フローを完遂させる「指揮者」です。

### アプリケーションサービスの責務

-   **トランザクション境界の定義**: どこからどこまでが一つのアトミックな操作であるかを定義します（例：`@Transactional`アノテーション）。
-   **集約の取得と保存**: リポジトリを介して、必要な集約をデータベースから取得し、処理が終わった集約を保存します。
-   **ドメインロジックの委譲**: **アプリケーションサービス自身はビジネスルールを知りません。** 関連する集約のメソッドを呼び出し、ドメインロジックの実行を「委譲」します。
-   **入力と出力の調整**: 外部からの入力（DTOなど）をドメインオブジェクト（値オブジェクトなど）に変換し、実行結果を外部向けの形式で返却します。
-   **イベントの発行**: 集約が記録したドメインイベントを、イベントパブリッシャーを介してシステム全体に通知します。

### コード例：`LoanApplicationService`

`guide04`で見た`LoanApplicationService`は、アプリケーションサービスの典型的な例です。

```java
// application/LoanApplicationService.java
package com.example.ddd.application;

// ... imports

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional // 1. トランザクション境界を定義
    public LoanApplicationId submitApplication(SubmitLoanRequest request) {
        
        // 4. 入力(DTO)をドメインオブジェクトに変換
        CustomerId customerId = new CustomerId(request.customerId());
        Money amount = new Money(request.amount());

        // 3. ドメインロジックの実行を集約に委譲
        LoanApplication application = LoanApplication.submit(customerId, amount);
        
        // 2. 集約をリポジトリに保存
        loanApplicationRepository.save(application);
        
        // 5. ドメインイベントを発行
        eventPublisher.publish(application.getDomainEvents());
        
        return application.getId();
    }
}
```
この通り、アプリケーションサービスは「何をするか（What）」を調整しますが、「どうやるか（How）」の詳細は知りません。その詳細はドメインオブジェクトにカプセル化されています。

## 10.2. ドメインサービス - 純粋なドメインロジックの置き場所

一方、**ドメインサービス (Domain Service)** は、ドメインロジックの一部でありながら、特定のエンティティや値オブジェクトの責務として自然に配置できない場合に利用されます。

### ドメインサービスの出番

ドメインサービスが必要になるのは、主に以下のようなケースです。

1.  **複数の集約が関わる操作**: ある操作が、複数の集約インスタンスを必要とする場合。例えば、「口座間振込」は、送金元口座と送金先口座という2つの`Account`集約を操作します。このロジックをどちらか一方の集約に持たせるのは不自然です。
2.  **外部サービスとの連携を含む計算**: ドメインロジックの計算過程で、外部のサービス（例：為替レートAPI、信用情報機関API）を呼び出す必要がある場合。集約自身が外部I/Oに依存するのは望ましくありません。
3.  **集約の生成ロジックが複雑な場合**: 新しい集約を生成する前に、リポジトリに問い合わせて何らかの検証（例：重複チェック）を行う必要がある場合。

重要なのは、ドメインサービスは**状態を持たない（ステートレス）**であるべき、という点です。

### コード例：`CustomerDuplicationValidator`

「顧客を新規登録する際に、同じメールアドレスの顧客が既に存在しないかチェックする」というロジックを考えてみましょう。このロジックは、新しい`Customer`集約が作られる「前」に実行される必要があり、リポジトリへの問い合わせを伴います。これはドメインサービスの典型的なユースケースです。

```java
// domain/service/CustomerDuplicationValidator.java
package com.example.ddd.domain.service;

import com.example.ddd.domain.model.customer.Customer;
import com.example.ddd.domain.model.customer.CustomerRepository;
import com.example.ddd.domain.model.customer.EmailAddress;

@Service // こちらはドメイン層のサービスとして定義
@RequiredArgsConstructor
public class CustomerDuplicationValidator {
    
    private final CustomerRepository customerRepository;

    public void validate(EmailAddress email) {
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new CustomerDuplicationException("Customer with email " + email.getValue() + " already exists.");
        }
    }
}

// アプリケーションサービスでの利用例
// application/CustomerRegistrationService.java
@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {
    
    private final CustomerRepository customerRepository;
    private final CustomerDuplicationValidator duplicationValidator;

    @Transactional
    public CustomerId register(String name, String emailValue) {
        EmailAddress email = new EmailAddress(emailValue);

        // 1. ドメインサービスを呼び出して重複チェック
        duplicationValidator.validate(email);
        
        // 2. チェックをパスしたら、新しい集約を生成
        Customer customer = Customer.register(name, email);
        
        // 3. 保存
        customerRepository.save(customer);
        
        return customer.getId();
    }
}
```

## 10.3. 比較まとめ

| 関心事               | アプリケーションサービス                     | ドメインサービス                                 |
| ------------------ | ---------------------------------------- | ---------------------------------------------- |
| **主な責務**         | ユースケースの調整、トランザクション管理     | 複数の集約にまたがるドメインロジック             |
| **状態**             | 持たない (ステートレス)                      | 持たない (ステートレス)                          |
| **ビジネスロジック** | 含まない（ドメインオブジェクトに委譲する）   | **含む**                                       |
| **配置場所**         | **アプリケーション層**                     | **ドメイン層**                                 |
| **依存方向**         | ドメイン層、インフラ層のIFに依存           | ドメイン層の他のオブジェクトにのみ依存           |

---

この2つのサービスを適切に使い分けることで、アプリケーション層は純粋にユースケースのフロー制御に集中でき、ドメイン層は純粋なビジネスロジックのカプセル化に集中できます。この明確な責務分離が、変化に強く、理解しやすいシステムを構築するための基礎となるのです。 