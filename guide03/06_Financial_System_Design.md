# 第1章: 金融システムにおけるドメイン駆動設計（DDD）の必然性

## 1.1. なぜ金融ドメインでDDDが不可欠なのか

金融システムは、その本質から複雑です。多様な金融商品、絶えず変化する規制、高いセキュリティ要件、そして何よりも精度への絶対的な要求が、開発に特有の課題をもたらします。従来の開発アプローチでは、この複雑性に対応しきれず、ビジネス要件と乖離した、保守困難なシステムを生み出すリスクがありました。

ドメイン駆動設計（DDD）は、この複雑性の核心に取り組むための戦略的なアプローチです。DDDは、ソフトウェアの設計をビジネスドメイン（業務領域）の知識に深く根ざさせ、ドメインエキスパートと開発者が**ユビキタス言語**という共通言語を築きながら、ビジネスの現実をソフトウェアモデルに正確に反映させることを目指します。

金融ドメインにおいてDDDが不可欠である理由は以下の通りです。

-   **複雑性の管理**: DDDは、大規模なドメインを**境界づけられたコンテキスト**に分割することで、管理可能な単位で複雑な問題に取り組むことを可能にします。
-   **ビジネスへの適応性**: 金融規制や市場環境は常に変化します。DDDはビジネスロジックをドメインモデル内にカプセル化するため、変更への適応性が向上し、保守コストを削減します。
-   **リスクの軽減**: クラス設計がビジネスの概念を直接反映するため、誤解によるバグや設計ミスを減らし、システムの堅牢性と信頼性を高めます。

## 1.2. 豊かなドメインモデルへの移行

DDDが目指すのは、データとそのデータを操作するロジックが一体となった**「豊かなドメインモデル」**の構築です。これは、データのみを保持し、振る舞いを持たない「貧血ドメインモデル」のアンチパターンとは対極にあります。金融システムにおいて豊かなドメインモデルを構築することは、ビジネスルールがドメインオブジェクト自身によって直接強制されることを意味し、システムの信頼性を飛躍的に向上させます。

---

# 第2章: 金融ドメインのための戦術的設計パターン

戦術的DDDは、ドメインモデルを具体的に構築するためのビルディングブロックを提供します。

## 2.1. エンティティ (Entity)

**エンティティ**は、一意の識別子によって区別され、そのライフサイクルを通じて同一性が維持されるオブジェクトです。

-   **特徴**: 一意のID、可変性、ライフサイクル、履歴を持つ。
-   **金融システムの例**:
    -   `Account` (口座): `AccountNumber` で識別され、残高やステータスが変化する。
    -   `Customer` (顧客): `CustomerID` で識別され、連絡先情報などが変化する。
    -   `Loan` (ローン): `LoanID` で識別され、元本や利率が変化する。

## 2.2. 値オブジェクト (Value Object)

**値オブジェクト**は、その属性によって定義され、識別子を持たない**不変の**オブジェクトです。

-   **特徴**: IDなし、不変、属性値に基づく等価性、自己検証。
-   **金融システムの例**:
    -   `Money` (金額): `amount` (金額)と `currency` (通貨)をカプセル化。不変であるため計算の安全性が高い。
    -   `InterestRate` (利率): パーセンテージ値を保持し、常に有効な範囲内であることを保証する。
    -   `Address` (住所): 顧客や取引の場所を表す。

金融ドメインでは、`Money` や `InterestRate` のような概念を値オブジェクトとして設計し、「構築時に正しい」状態を強制することが、バグを減らしシステムの信頼性を高める上で極めて重要です。

## 2.3. アグリゲート (Aggregate)

**アグリゲート**は、関連するエンティティと値オブジェクトを「一貫性の境界」としてまとめたクラスターです。

-   **特徴**:
    -   **アグリゲートルート**が外部からの唯一のアクセスポイントとなる。
    -   アグリゲート内の不変条件（ビジネスルール）を強制する。
    -   変更はアトミックなトランザクションとして扱われる。
-   **金融システムの例**:
    -   **`Account` 集約**: ルートは `Account` エンティティ。内部に `Transaction` のリストや `Balance` (残高) を保持し、「残高はマイナスにできない」といったルールを強制する。
    -   **`LoanApplication` 集約**: ルートは `LoanApplication` エンティティ。内部に申請者の詳細や要求額を保持する。

DDDの重要な原則として、**「一つのトランザクションで変更してよいのは、一つのアグリゲートインスタンスのみ」**というルールがあります。これにより、アグリゲート内の強力な一貫性が保証されます。

## 2.4. ドメインサービス (Domain Service)

単一のエンティティや値オブジェクトに自然に属さないドメインロジックは、**ドメインサービス**にカプセル化されます。

-   **特徴**: ステートレスであり、複数のアグリゲートを調整する操作を担当する。
-   **金融システムの例**:
    -   `FundTransferService` (資金振替サービス): 2つの `Account` 集約間の資金移動を調整する。
    -   `RiskAssessmentService` (リスク評価サービス): 複数の情報源から顧客のリスクを計算する。

```java
/*-- ドメイン層：Account集約 --*/
public class Account extends AggregateRoot {
    private AccountId id;
    private Money balance;

    public void withdraw(Money amount) {
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientBalanceException("残高が不足しています。");
        }
        this.balance = this.balance.subtract(amount);
        // FundsWithdrawnイベントを登録する
        registerEvent(new FundsWithdrawn(this.id, amount, Instant.now()));
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
        // FundsDepositedイベントを登録する
        registerEvent(new FundsDeposited(this.id, amount, Instant.now()));
    }
    // ...
}

/*-- ドメイン層：FundTransferServiceドメインサービス --*/
public class FundTransferService {

    public void transfer(Account sourceAccount, Account destinationAccount, Money amount) {
        // ドメインのビジネスロジックを集約のメソッドに委譲する
        sourceAccount.withdraw(amount);
        destinationAccount.deposit(amount);
    }
}

/*-- アプリケーション層：ApplicationService --*/
@Service
public class TransferApplicationService {
    
    private final AccountRepository accountRepository;
    private final FundTransferService fundTransferService;
    private final DomainEventPublisher eventPublisher;

    // ... コンストラクタ ...

    @Transactional
    public void transferFunds(AccountId sourceId, AccountId destId, Money amount) {
        // 1. 必要な集約を取得
        Account source = accountRepository.findById(sourceId).orElseThrow();
        Account dest = accountRepository.findById(destId).orElseThrow();

        // 2. ドメインサービスに処理を委譲
        fundTransferService.transfer(source, dest, amount);

        // 3. 結果を保存
        accountRepository.save(source);
        accountRepository.save(dest);
        
        // 4. イベントを発行
        eventPublisher.publish(source.pollDomainEvents());
        eventPublisher.publish(dest.pollDomainEvents());
    }
}
```

## 2.5. リポジトリ (Repository) とファクトリ (Factory)

-   **リポジトリ**: ドメインモデルと永続化層（データベースなど）の間の抽象化を提供し、アグリゲートの保存と取得を管理します。
-   **ファクトリ**: 複雑なオブジェクト（特にアグリゲート）の生成ロジックをカプセル化し、常に有効な状態でオブジェクトが作成されることを保証します。

---

# 第3章: DDDとマイクロサービスアーキテクチャ

DDDの原則、特に**境界づけられたコンテキスト**は、金融システムをマイクロサービスに分割するための理想的な設計図となります。

## 3.1. 境界づけられたコンテキストからマイクロサービスへ

**境界づけられたコンテキスト（BC）**は、特定のドメインモデルと言語が適用される明確な境界です。この境界が、マイクロサービスの境界の主要な候補となります。

-   **金融ドメインにおけるBCの例**:
    -   支払決済コンテキスト
    -   融資審査コンテキスト
    -   顧客管理コンテキスト
    -   リスク評価コンテキスト

各BCを一つのマイクロサービスとして実装することで、サービス内の高い凝集性と、サービス間の疎結合を実現し、技術スタックの柔軟性や独立したデプロイを可能にします。

## 3.2. 集約をまたぐ一貫性：ドメインイベントと結果整合性

単一のアグリゲート内ではトランザクションによる強い一貫性が保証されますが、複数のマイクロサービス（＝複数のアグリゲート）にまたがる操作では、**ドメインイベント**と**結果整合性（Eventual Consistency）**のアプローチが一般的です。

例えば、「口座間振替」という操作は以下のように実現されます。
1.  振替元 `Account` 集約から資金を引き落とす。
2.  成功したら `FundsWithdrawn` (資金引落済み) ドメインイベントを発行する。
3.  別のサービスがこのイベントを購読し、非同期で振替先 `Account` 集約に資金を追加入金する。

このアプローチは、システム全体のパフォーマンスとスケーラビリティを向上させる上で不可欠です。

---

# 第4章: 実装上の重要な考慮事項

## 4.1. 並行性制御：楽観的ロック vs 悲観的ロック

金融システムでは、複数のプロセスが同時に同じデータにアクセスする際の整合性維持が重要です。

-   **悲観的ロック**: データアクセス時にリソースをロックし、他からの変更を防ぎます。一貫性は強力に保証されますが、パフォーマンスのボトルネックになる可能性があります。大規模な資金移動など、絶対に競合を避けたい場合に適しています。
-   **楽観的ロック**: データ更新時にバージョニングなどを用いて競合を検出し、競合があれば処理をロールバックします。競合が少ない場合に高いパフォーマンスを発揮します。

多くの場合、システムの特性に応じて両者を使い分けるハイブリッドなアプローチが有効です。

## 4.2. 段階的なモダナイゼーション

既存の巨大なレガシーシステムを一度に刷新するのは非現実的です。**ストラングラーフィグパターン**などを利用し、モノリスの機能を段階的に新しいマイクロサービスで置き換えていくアプローチが推奨されます。その際、新旧システム間の連携には**防護層 (Anti-Corruption Layer)** の導入が有効です。 