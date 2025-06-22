

# **ドメイン駆動設計（DDD）テスト戦略の決定版ガイド：基本原則から実装ブループリントまで**

## **第1章 DDDとテストの共生関係**

ドメイン駆動設計（DDD）におけるテストは、単なる品質保証活動ではありません。それは、設計とモデリングプロセスの不可欠な一部であり、ソフトウェアがビジネスを正確に反映していることを検証する中心的メカニズムです。この章では、DDDの基本理念がどのようにテスト戦略を形成し、従来のテストアプローチとは一線を画す、より価値の高いフィードバックループを生み出すのかを明らかにします。

### **1.1 検証を超えて：設計ツールとしてのテスト**

ドメイン駆動設計の核心は、ソフトウェアが解決しようとするドメイン、すなわち特定の問題領域に焦点を当てることにあります 1。ソフトウェアはユーザーの問題を解決するために存在するため、そのテスト戦略もまた、この根本的な目的に沿って構築されなければなりません 1。  
従来のテストアプローチは、しばしば技術的な実装詳細の検証に重きを置きます。例えば、データベースの特定のカラムに正しい値が書き込まれたか、特定の関数が期待された回数呼び出されたか、といった点です。これに対し、DDDにおけるテストは、ドメインモデルにカプセル化されたビジネスルールと振る舞いを検証することに主眼を置きます 3。このパラダイムシフトは、テストを単なるバグ発見ツールから、ドメインモデルの正確性と表現力を検証し、洗練させるための設計ツールへと昇華させます。  
このアプローチによって書かれたテストは、実装のリファクタリングに対して非常に強くなります。なぜなら、テストは「どのように実装されているか」ではなく、「ビジネスとして何をすべきか」を検証しているからです。結果として、テストスイートはビジネスにとってより価値のある資産となり、ソフトウェアの進化を安全にサポートします。

### **1.2 ユビキタス言語：テスト可能な語彙**

DDDの成功に不可欠な要素が「ユビキタス言語」です。これは、ドメインエキスパート（業務の専門家）と開発者が共有し、プロジェクトのあらゆる場面で一貫して使用する語彙体系を指します 3。この共通言語は、コミュニケーションの齟齬をなくし、要件の誤解を減らす上で極めて重要です 6。  
このユビキタス言語は、テスト戦略において直接的かつ強力な役割を果たします。テストのシナリオ、テストケース名、アサーション（検証）ロジックは、すべてユビキタス言語の用語を用いて記述されるべきです。これにより、テストスイートは単なるコードの検証ツールではなく、ドメインのルールと振る舞いを記述した「生きた実行可能なドキュメント」へと進化します 8。  
例えば、著名な専門家であるVladimir Khorikovは、テスト名を「そのドメインに詳しい非プログラマーにシナリオを説明するかのように」命名することを推奨しています 9。  
test\_order\_is\_marked\_as\_shipped\_when\_payment\_is\_confirmed（支払いが確認されたら注文は発送済みとしてマークされることをテストする）のようなテスト名は、ビジネス関係者にとっても即座に理解可能であり、その正当性を検証できます。  
この実践は、ドメインモデルの健全性を測るための強力な診断ツールとしても機能します。もし開発者がビジネス用語を使ってテストを記述することに困難を感じる場合、それはテスト自体の問題ではなく、ドメインモデルの根本的な弱さを示唆している可能性が高いです。例えば、test\_user\_status\_field\_is\_set\_to\_3（ユーザーステータスフィールドが3に設定されることをテストする）といった技術的な実装詳細を検証するテストしか書けないのであれば、それはdeactivate\_user\_after\_90\_days\_of\_inactivity（90日間非アクティブだったユーザーを無効化する）といったビジネス概念を直接表現するメソッドがモデルに欠けていることを示しています。これは、ビジネスロジックがモデルの外に漏れ出している「貧血ドメインモデル（Anemic Domain Model）」の典型的な兆候です。このように、テストの記述しやすさは、ユビキタス言語とドメインモデルの質を直接的に反映するバロメーターとなるのです。

### **1.3 モデル駆動設計とフィードバックループ**

DDDは、ドメインエキスパートとの対話を通じて得られた知識を、継続的にモデルに「凝縮（crunching knowledge into models）」していく反復的なプロセスです 10。このアプローチでは、モデルとコードは常に密接に結びつけられ、モデルの変更は即座にコードに反映され、逆にコーディング中に得られた新たな洞察はモデルにフィードバックされます 10。  
この「モデル駆動設計」のサイクルを支えるのが、自動化されたテストです。高品質なテストスイートは、開発者に迅速なフィードバックを提供し、ドメインの理解が深まるにつれてモデルをリファクタリングする自信を与えます。ビジネスルールが破壊されていないことを常に確認できるため、開発チームは大胆かつ安全に設計の改善を進めることができ、結果として変更に強く、保守性の高いシステムが実現します 4。

## **第2章 DDDにおけるテスト容易性のためのアーキテクチャ設計**

ドメイン駆動設計（DDD）におけるアーキテクチャの選択は、単なるコードの整理術ではありません。それは、効果的で持続可能なテスト戦略を実現するための根幹をなす、極めて戦略的な決定です。優れたアーキテクチャは、ドメインの関心事を保護し、各コンポーネントを独立してテスト可能にすることで、DDDのポテンシャルを最大限に引き出します。

### **2.1 依存関係逆転の原則：テスト容易性の礎**

DDDを実践する上で採用される代表的なアーキテクチャには、レイヤードアーキテクチャ、オニオンアーキテクチャ、そしてヘキサゴナルアーキテクチャ（ポート＆アダプター）があります 13。これらのアーキテクチャに共通する最も重要な特徴は、依存関係の方向性です。すなわち、すべての依存関係はシステムの中心にあるドメイン層に向かって一方向に流れます 13。  
この原則の核心は、ドメイン層の「純粋性」を保つことにあります。ドメイン層は、ビジネスのルールとロジックを表現することに専念し、アプリケーション層やインフラストラクチャ層といった外部の関心事（例：UI、データベース、外部API）に一切依存してはなりません 14。この厳格な分離が、ドメイン層を独立してテストするための鍵となります。

### **2.2 レイヤーのテスト：戦略的ブレークダウン**

アーキテクチャをレイヤーに分割することで、各レイヤーの責任が明確になり、それぞれに最適化されたテスト戦略を適用できます。

* ドメイン層のテスト  
  ドメイン層は、アーキテクチャの中心に位置し、最も複雑なビジネスロジックを内包します。この層は、外部の技術的詳細から完全に隔離され、「永続化を意識しない（persistence ignorant）」状態にあります 17。この特性により、ドメイン層はプレーンなオブジェクトとしてメモリ上で完結したテストが可能です。これは、DDDがもたらす最大の利点の一つであり、システムの心臓部であるビジネスロジックに対して、高速かつ信頼性の高い網羅的な単体テスト（ユニットテスト）を実施することを可能にします 4。  
* アプリケーション層のテスト  
  アプリケーション層は、特定のユースケースを実現するために、ドメインオブジェクトとインフラストラクチャサービスを調整（オーケストレーション）する役割を担います 15。この層のテストは、この調整役としての振る舞いが正しいかを検証します。テストでは、ドメインオブジェクトはそのまま実物を使用し、リポジトリやイベント発行などのインフラストラクチャへのインターフェースは、テストダブル（スタブやフェイクなど）に置き換えるのが一般的です 17。  
* インフラストラクチャ層のテスト  
  インフラストラクチャ層は、リポジトリの具象クラスや外部APIとの通信クライアントなど、具体的な技術実装を含みます。この層のテストは、外部技術との連携を検証する統合テスト（インテグレーションテスト）となります。例えば、データベースとのマッピングが正しく機能するか、API呼び出しが期待通りに行われるかなどを検証します 17。これらのテストはドメイン層のテストに比べて実行速度が遅いため、数は少なくなりますが、システムの完全性を保証する上で不可欠です。

### **2.3 境界づけられたコンテキストの役割とテストスコープ**

戦略的設計は、システムを「境界づけられたコンテキスト（Bounded Context）」と呼ばれる、それぞれが独自のモデルとユビキタス言語を持つ論理的な区画に分割します 2。この分割は、テストのスコープを定義する上で自然な境界を提供します。  
原則として、テストは単一の境界づけられたコンテキスト内で完結させるべきです。コンテキスト間の連携（例えば、公開されたイベントの購読や、腐敗防止層（Anti-Corruption Layer）を介した通信）は、統合テストの対象となります。特に、コンテキスト間のAPIコントラクト（契約）を検証する「契約テスト（Contract Testing）」は、分散システムにおける連携の安定性を保証する上で非常に有効な手法です 2。  
アーキテクチャの選択は、記述できるテストの種類と価値に直接的な影響を及ぼします。「古典的な」レイヤードアーキテクチャと、オニオンアーキテクチャやヘキサゴナルアーキテクチャとでは、後者の方がDDDのテスト戦略において優れていると考えられます。その理由は、後者の方がドメインの純粋性をより厳格に強制するためです。古典的なN層アーキテクチャでは、ドメイン層がインフラ層で定義されたインターフェース（例：IRepository）に依存することが許容されがちです 16。これに対し、オニオンやヘキサゴナルアーキテクチャでは、  
IRepositoryのようなインターフェース（ポート）はドメイン層またはアプリケーション層の*内部*で定義されます。そして、インフラストラクチャ層の具象クラス（アダプター）がそのポートを*実装*します 14。この厳格な依存関係逆転により、ドメイン層は自身が必要とするものを定義するだけで、それがどのように実現されるかについては一切関知しません。この「真の分離」が、インフラストラクチャの関心事がドメインモデルを汚染することを防ぎ、結果としてドメインの単体テストを一切のテストダブルなしで記述することを保証します。これこそが、DDDのテスト戦略が目指す、価値と信頼性の高いテストスイートの基盤となるのです。

## **第3章 ドメイン駆動システムのためのテストピラミッド再考**

テスト戦略を議論する上で、Mike Cohn氏が提唱した「テストピラミッド」は広く知られたモデルです 20。このモデルは、ドメイン駆動設計（DDD）の文脈において、単なる理論的な理想像ではなく、優れた設計によって自然と達成されるべき構造として再解釈することができます。本章では、DDDがどのようにして健全なテストピラミッドの実現を促進するのかを詳述します。

### **3.1 古典的なテストピラミッド**

テストピラミッドの基本的な考え方は、テストの種類を3つの階層に分類し、それぞれの量にバランスを持たせるというものです 20。

* **基盤（Unit Tests）：** ピラミッドの最も広い土台を形成するのは、高速で独立した単体テストです。これらはコードの小さな単位を対象とし、数が最も多くなります。  
* **中間層（Integration Tests）：** 複数のコンポーネント間の連携や、外部システムとの接続を検証する統合テストです。単体テストより数は少なく、実行速度も遅くなります。  
* **頂点（End-to-End Tests）：** ユーザーインターフェース（UI）からデータベースまで、システム全体を貫通するエンドツーエンド（E2E）テストです。最も実行コストが高く、不安定になりやすいため、その数は最小限に抑えるべきです。

このピラミッドの目的は、テストを可能な限り下層に押し下げることで、迅速なフィードバックと高い信頼性を両立させることにあります。E2Eテストに過度に依存する「アイスクリームコーン」型のアンチパターンは、ビルド時間の増大とテストの不安定化を招くため、避けるべきとされています 21。

### **3.2 DDDに整合したピラミッド：広く価値ある基盤**

適切に設計されたDDDシステムでは、テストピラミッドは自然な形で形成されます。その理由は、システムの最も複雑なビジネスロジックが、完全に分離されたドメイン層に集約されているためです 17。前述の通り、このドメイン層は外部依存から切り離されているため、網羅的な単体テストに非常に適しています 24。  
結果として、DDDアプローチは、システムの核心的価値であるビジネスルールをカバーする、非常に幅広く価値の高い単体テストの基盤を自然に構築します。これは、テストピラミッドが理想とする姿そのものです 23。一部では、統合テストを重視する「テストダイヤモンド」のようなモデルも議論されますが 25、純粋なドメインモデルを持つDDDにおいては、ピラミッド型が最も適合するテスト戦略と言えるでしょう。

### **3.3 DDDコンテキストにおけるテストタイプの定義**

DDDの文脈でテストピラミッドを適用する際、各階層のテストをより具体的に定義することが重要です。

* **単体テスト（Unit Tests）：** 主にドメイン層（集約、値オブジェクト、ドメインサービス）を完全な分離状態でテストします。これらは高速かつ信頼性が高く、テストスイートの大部分を構成します 20。  
* **統合テスト（Integration Tests）：** レイヤー間の協調動作を検証します。主な例は以下の通りです。  
  * リポジトリをフェイクやスタブに置き換えた状態でのアプリケーションサービスのテスト。  
  * リポジトリ実装と実際のデータベース（テスト用に分離されたインメモリDBやコンテナ化されたDB）との連携テスト 17。  
  * 境界づけられたコンテキスト間の通信（イベントの消費など）のテスト。  
* **エンドツーエンド（E2E）/フィーチャーテスト（Feature Tests）：** UIやAPIのエンドポイントからデータベースに至るまで、特定のユーザーストーリーや機能の全体的な流れをテストします 17。これらは最もコストが高いため、システムの根幹をなす重要な「ハッピーパス」シナリオに限定して実施されるべきです。

DDDを実践する中で、「単体テスト」と「統合テスト」の境界は曖昧になりがちです。このため、より実践的な分類として「Solitary（孤独な）テスト」と「Sociable（社交的な）テスト」、あるいは「古典学派（Classical）」と「モック主義者（Mockist）」という考え方が登場します。重要なのはテストのラベルではなく、「何を隔離しているか」という本質を理解することです。  
例えば、伝統的な単体テストの定義では、1つのクラスをテストし、その依存関係はすべてモック化します 26。しかしDDDでは、「集約（Aggregate）」が概念的なテスト単位と見なされます 17。集約ルートのテストにおいて、その内部に存在するエンティティや値オブジェクトの実インスタンスを使用する場合、これはドメインの観点からは「単体テスト」ですが、厳密なクラス単位の視点では「社交的なテスト」あるいは「統合テスト」と見なせます。  
この区別は、Martin Fowler氏やVladimir Khorikov氏といった専門家によっても広く議論されています 21。「古典学派（デトロイト派）」は、実際の協調オブジェクト（コラボレーター）を使用する社交的なテストを好み、「モック主義者（ロンドン派）」はすべての協調オブジェクトをモック化する孤独なテストを好みます 26。  
DDDのドメイン層においては、圧倒的に古典学派／社交的なアプローチが優れています。集約を、その内部にある実際の（メモリ上の）子オブジェクトと共にテストすることは、それらをモック化するよりもはるかに高い価値と忠実性を提供します。したがって、最も実践的なアプローチは、「単体」か「統合」かというラベルに固執するのをやめ、「テスト対象コンポーネントの境界はどこか？その振る舞いに不可欠な依存関係と、外部の協調オブジェクトはどれか？」と自問することです。集約にとって、その内部オブジェクトは不可欠です。アプリケーションサービスにとって、リポジトリは外部の協調オブジェクトです。この洗練された視点こそが、成熟したテスト戦略を築く上で不可欠なのです。

## **第4章 DDDビルディングブロックの戦術的テストガイド**

この章では、DDDの戦術的設計パターンそれぞれに対する、具体的で実践的なテスト戦略を詳述します。理論を実践に移すための、本レポートの核心部分です。

### **4.1 値オブジェクト（Value Objects）のテスト**

* **焦点：** 不変性（Immutability）と値に基づいた等価性（Value-based Equality）。  
* **戦略：**  
  * **不変条件の検証：** コンストラクタのロジックが、オブジェクトの不変条件を正しく強制していることを検証します。例えば、EmailAddressオブジェクトが無効な形式の文字列で生成できないことを確認します 28。  
  * **等価性の検証：** 同じ基底値を持つ2つの値オブジェクトが等しいと見なされること（equals()がtrueを返すこと）、そして同じハッシュコードを持つことをアサートします 29。逆に、異なる値を持つインスタンスが等しくないと評価されることも確認します。  
  * **不変性の検証：** publicなセッターが存在しないことを確認します。オブジェクトを「変更」するように見えるメソッド（例：Moneyオブジェクトの加算）が、実際には*新しい*インスタンスを返し、元のインスタンスは変更されていないことを検証します 29。

### **4.2 エンティティ（Entities）と集約（Aggregates）のテスト**

* **焦点：** トランザクションの整合性とビジネスルールの強制。  
* **戦略：**  
  * **集約全体をテスト単位とする：** テストの単位は**集約全体**です 17。集約の内部に存在するエンティティを個別にテストしてはいけません。それらは集約の一部としてのみ意味を持つからです 32。  
  * **集約ルートを介した操作：** テストは、**集約ルート**のpublicメソッドを通じてのみ集約と対話すべきです 31。これにより、集約の不変条件が一貫して守られることを保証します。  
  * **Arrange-Act-Assertパターンの適用：**  
    * **Arrange（準備）：** 集約を特定の初期状態にします。これはファクトリを用いるか、集約自身のメソッドを連続して呼び出すことで行います。ファクトリの使用が堅牢ですが、直接的な状態設定も注意深く行えば有効です 17。  
    * **Act（実行）：** テスト対象のビジネス操作を表す単一のpublicメソッドを呼び出します（例：order.addItem(product)）。  
    * **Assert（検証）：** 結果を検証します。これには2つの側面があります。  
      1. **状態の検証：** 集約の状態が正しく変更されたことを確認します（例：order.totalPrice()が新しい商品の価格を反映しているか）。  
      2. **振る舞いの検証：** 期待される**ドメインイベント**が発行されたことを確認します（例：ItemAddedToOrderEventが記録されているか） 31。

### **4.3 ドメインサービス（Domain Services）のテスト**

* **焦点：** 複数の集約にまたがる操作や、特定の集約に属さない複雑な計算。  
* **戦略：**  
  * ドメインサービスはステートレスであるべきです 34。テストはサービス自体の内部状態に依存してはなりません。  
  * ドメインサービスをインスタンス化し、必要な入力（通常は1つ以上の集約や値オブジェクト）を与えます。これらの入力は実オブジェクトでもテストダブルでも構いません。  
  * サービスのメソッドを呼び出し、返された結果（計算値など）や、引数として渡された集約の状態変化をアサートします。

### **4.4 リポジトリ（Repositories）のテスト**

* **焦点：** 永続化マッピングとクエリロジックの検証。  
* **戦略：**  
  * これらは**統合テスト**であり、単体テストではありません。Testcontainersのようなツールや、インメモリDB（H2、SQLiteなど）を用いて、実際のデータベースエンジンに対して実行すべきです。ただし、インメモリDBは本番環境と挙動が異なる可能性がある点に注意が必要です 26。  
  * ORM（Object-Relational Mapper）やデータベースドライバをモック化してはいけません。それではテストする意味がほとんどなくなってしまいます 17。  
  * テストサイクルは以下のようになります。  
    1. 集約のインスタンスを生成します。  
    2. リポジトリを使って集約を保存します（add/update）。  
    3. リポジトリを使ってIDで集約を再取得します。  
    4. 再取得した集約が、元の集約と状態的に同一であることをアサートします。  
  * クエリメソッドのテストでは、テストデータを複数保存し、クエリを実行して、期待される集約が正しく返されることを検証します。

### **4.5 アプリケーションサービス（Application Services / Use Cases）のテスト**

* **焦点：** ビジネスユースケースのオーケストレーション（調整役）。  
* **戦略：**  
  * これらも通常は統合テストですが、リポジトリのテストとは境界が異なります。テストの単位はユースケースです 17。  
  * リポジトリ、決済ゲートウェイ、イベント発行者など、ポートによって定義された外部依存関係をモック、スタブ、またはフェイクに置き換えます 17。特にリポジトリに対しては、インメモリのフェイク実装が非常に有効です。  
  * テストフローは以下のようになります。  
    1. テストダブル（モックやスタブ）を準備します（例：mockRepository.when(findById(x)).thenReturn(aggregate)）。  
    2. アプリケーションサービスを、準備したテストダブルを注入してインスタンス化します。  
    3. コマンドオブジェクトやDTOを引数として、サービスのメソッドを呼び出します。  
    4. 協調オブジェクトの正しいメソッドが呼び出されたこと（例：verify(mockRepository).save(any(Aggregate.class))）や、期待される結果が返されたことを検証します。

### **DDDコンポーネントのテスト戦略マトリクス**

以下の表は、各DDDビルディングブロックに対するテスト戦略の要点をまとめたものです。開発者が一目で各コンポーネントのテスト方針を理解し、チーム内での一貫性を保つためのクイックリファレンスとして機能します。

| コンポーネント | テスト種別 | テスト単位 | 隔離対象の依存関係 | 主な検証焦点 |
| :---- | :---- | :---- | :---- | :---- |
| **値オブジェクト** | 単体テスト | クラス単体 | なし | 不変性、値による等価性、不変条件 |
| **エンティティ/集約** | 単体テスト | 集約全体 | 他の集約（必要な場合） | ビジネスルール、状態遷移、ドメインイベント発行 |
| **ドメインサービス** | 単体テスト | クラス単体 | なし（ステートレス） | 複数の集約にまたがる計算・操作ロジック |
| **リポジトリ** | 統合テスト | クラス単体 | なし（DBと連携） | 永続化マッピング、クエリの正確性 |
| **アプリケーションサービス** | 統合テスト | ユースケース | インフラ層のインターフェース（リポジトリ、外部サービス等） | ユースケースの調整ロジック、協調オブジェクトとのインタラクション |

## **第5章 テストダブルの習得：DDDにおけるモック、スタブ、フェイク**

テストダブルは、テスト対象のコードをその依存関係から隔離するための強力なツールです。しかし、その使い方を誤ると、テストは脆く、保守が困難なものになってしまいます。この章では、テストダブルの各種（モック、スタブ、フェイクなど）を明確に定義し、DDDの文脈でそれぞれをいつ、どのように使用すべきかという原則に基づいた、洗練されたアプローチを提示します。

### **5.1 明確な分類法：Dummy, Stub, Fake, Spy, Mock**

まず、Gerard Meszaros氏の古典的な定義に基づき、共通の語彙を確立します 36。

* **Dummy（ダミー）：** パラメータリストを埋めるためだけに渡され、実際には使用されないオブジェクト。nullなどが該当します。  
* **Stub（スタブ）：** テスト中の呼び出しに対して、あらかじめ定義された「缶詰の答え」を返すオブジェクト。主にテスト対象への入力（クエリの結果など）を提供するために使用されます。  
* **Spy（スパイ）：** スタブの機能に加え、どのように呼び出されたか（呼び出し回数や引数など）を記録する機能を持つオブジェクト。  
* **Mock（モック）：** 自身がどのように呼び出されるべきかという「期待」を事前にプログラムされたオブジェクト。期待通りの呼び出しがなければテストは失敗します。主にテスト対象からの出力（コマンドの実行）を検証するために使用されます。  
* **Fake（フェイク）：** 実際に動作する実装を持つが、本番環境での使用には適さない軽量な代替品。インメモリデータベースやインメモリリポジトリが典型例です 37。

### **5.2 中核となる違い：状態検証 vs 振る舞い検証**

スタブとモックの最も根本的な違いは、何を検証するかにあります。

* スタブは状態検証（State Verification）のために使用されます。  
  テストでは、スタブを使ってテスト対象（System Under Test, SUT）に特定のデータや状態を入力として提供します。その後、SUT自体の状態が期待通りに変化したか、あるいはSUTが正しい結果を返したかをアサートします。ここでの主役はSUTの状態です。  
* モックは振る舞い検証（Behavior Verification）のために使用されます。  
  テストでは、SUTがその依存オブジェクトに対して正しいメソッドを正しい引数で呼び出したか、という\*相互作用（インタラクション）\*を検証するためにモックを使用します。ここでの主役は、SUTと依存オブジェクト間のコミュニケーションです。

### **5.3 Khorikovの教義：いつ、何をモックすべきか**

このセクションでは、Vladimir Khorikov氏が提唱する影響力のある原則、通称「Khorikovの教義」を基に、モックの適切な使用法を掘り下げます 27。

* **原則：自分が所有していない型のみをモックする（"unmanaged dependencies"）。**

この原則を理解するためには、依存関係を2つのカテゴリに分類する必要があります。

* 管理外依存（Unmanaged Dependencies）：  
  これは、SMTPサーバー、決済代行サービスのAPI、メッセージバスなど、自分が管理・所有しておらず、外部の第三者によって提供されるシステムを指します。これらのシステムとの通信プロトコルは、我々のアプリケーションの公開された契約（コントラクト）の一部です。したがって、テストにおいて実際にメールを送信したり決済を行ったりすることなく、この契約が守られているかを検証するためにモックを使用することは不可欠かつ適切です 27。  
* 管理下依存（Managed Dependencies）：  
  これは、アプリケーション自身のデータベースなど、我々が完全にコントロールできる依存関係を指します。データベースとの通信方法は、外部に公開された契約ではなく、あくまで実装の詳細です。リポジトリをモックしてsave()メソッドが呼び出されたことを検証するようなテストは、リファクタリングによって容易に破壊されるため、非常に脆くなります。このような場合、モックを使うのではなく、インメモリのフェイク実装や、Testcontainersなどを使った本物のデータベースとの統合テストを通じて、「データが実際に保存された」という最終的な結果を検証する方がはるかに堅牢で価値が高いのです 27。

モックを使用するか、スタブやフェイクを使用するかの判断は、単なる技術的な選択ではありません。それは、システムの境界をどこに引くかという設計上の決定そのものです。モックは、その対象を「外部」とのインタラクションと定義づける行為です。内部コンポーネント（例えば、あるドメインサービスから別のドメインサービスを呼び出す場合など）に対して安易にモックを使用することは、アプリケーション内部の境界が曖昧であるか、コンポーネントが密結合しすぎていることの兆候です。  
モックを使ったテストが脆くなるという「痛み」は、コードベースからの「設計を見直せ」という重要なシグナルです 40。内部の依存関係をモックしたくなる衝動に駆られたとき、それは単にモックオブジェクトをインスタンス化するのではなく、コンポーネントの責任分割や結合度について再考するべき時なのです。

## **第6章 実装ブループリント：実践DDDテスト**

この章では、これまでに説明してきた抽象的な概念を具体的なコードに落とし込みます。Java、C\#、Pythonの各言語におけるテスト実装の例を通じて、理論と実践の架け橋となることを目指します。

### **6.1 Java (Spring Boot) での実装例**

Javaエコシステム、特にSpring Bootは、DDDの実装とテストをサポートする豊富な機能を提供します。

* リッチな集約のテスト (JUnit 5, AssertJ):  
  集約のテストでは、ビジネスロジックの実行後の状態変化とドメインイベントの発行を検証します。Article集約にpublish()メソッドがあると仮定します。  
  Java  
  // Article.java (抜粋)  
  public class Article extends AggregateRoot {  
      //... fields  
      public void publish() {  
          if (this.status\!= Status.DRAFT) {  
              throw new ArticleAlreadyPublishedException();  
          }  
          this.status \= Status.PUBLISHED;  
          this.publishedAt \= ZonedDateTime.now();  
          this.registerEvent(new ArticlePublishedEvent(this.id.value()));  
      }  
  }

  // ArticleTest.java (抜粋)  
  @Test  
  @DisplayName("下書き状態の記事を公開できる")  
  void should\_publish\_draft\_article() {  
      // Arrange  
      Article article \= Article.create(  
          new ArticleId("..."),  
          new ArticleTitle("DDD Testing"),  
          new ArticleContent("..."),  
          Status.DRAFT  
      );

      // Act  
      article.publish();

      // Assert  
      assertThat(article.getStatus()).isEqualTo(Status.PUBLISHED);  
      assertThat(article.getPublishedAt()).isNotNull();  
      assertThat(article.pullDomainEvents())  
         .hasSize(1)  
         .first()  
         .isInstanceOf(ArticlePublishedEvent.class);  
  }

  このテストは、publishメソッドによって状態（status, publishedAt）が正しく遷移し、ArticlePublishedEventが発行されたことを検証しています 41。  
* JPAリポジトリの統合テスト (Testcontainers):  
  リポジトリのテストは、実際のデータベースとの連携を確認するために不可欠です。Testcontainersは、テスト実行時にDockerコンテナを起動し、本番に近い環境を提供します。  
  Java  
  @DataJpaTest  
  @Testcontainers  
  @AutoConfigureTestDatabase(replace \= AutoConfigureTestDatabase.Replace.NONE)  
  class ArticleRepositoryIntegrationTest {

      @Container  
      static PostgreSQLContainer\<?\> postgres \= new PostgreSQLContainer\<\>("postgres:13");

      @Autowired  
      private ArticleRepository articleRepository;

      @Test  
      @DisplayName("記事を保存し、IDで取得できる")  
      void should\_save\_and\_find\_article() {  
          // Arrange  
          Article article \= Article.create(...);

          // Act  
          articleRepository.save(article);  
          Optional\<Article\> foundArticle \= articleRepository.findById(article.getId());

          // Assert  
          assertThat(foundArticle).isPresent();  
          assertThat(foundArticle.get().getId()).isEqualTo(article.getId());  
      }  
  }

  このテストは、Spring Data JPAリポジトリがPostgreSQLコンテナ上で正しく動作することを確認します 43。  
* アプリケーションサービスのテスト (Mockito):  
  アプリケーションサービスは、リポジトリなどの依存関係をモック化してテストします。  
  Java  
  @ExtendWith(MockitoExtension.class)  
  class ArticleCreatorServiceTest {  
      @Mock  
      private ArticleRepository articleRepository;  
      @InjectMocks  
      private ArticleCreatorService articleCreatorService;

      @Test  
      @DisplayName("記事作成ユースケースがリポジトリの保存メソッドを呼び出す")  
      void should\_call\_save\_on\_repository() {  
          // Arrange  
          CreateArticleCommand command \= new CreateArticleCommand("title", "content");  
          when(articleRepository.save(any(Article.class))).thenReturn(null);

          // Act  
          articleCreatorService.create(command);

          // Assert  
          verify(articleRepository, times(1)).save(any(Article.class));  
      }  
  }

  ここではMockitoを使い、ArticleCreatorServiceがarticleRepository.save()を呼び出すという振る舞いを検証しています 41。

### **6.2 C\# (.NET) での実装例**

.NETプラットフォームでは、xUnitやNUnitといったテストフレームワークと、Moqのようなモッキングライブラリが広く利用されています。

* 値オブジェクトのテスト (xUnit):  
  値オブジェクトのテストでは、等価性と不変性を中心に検証します。  
  C\#  
  // Email.cs (抜粋)  
  public record Email(string Value)  
  {  
      public Email {  
          if (string.IsNullOrWhiteSpace(Value) ||\!Value.Contains("@"))  
              throw new ArgumentException("Invalid email format");  
      }  
  }

  // EmailTests.cs  
  public class EmailTests  
  {  
      \[Fact\]  
      public void Two\_emails\_with\_same\_value\_should\_be\_equal()  
      {  
          // Arrange  
          var email1 \= new Email("test@example.com");  
          var email2 \= new Email("test@example.com");

          // Act & Assert  
          Assert.Equal(email1, email2);  
          Assert.True(email1 \== email2);  
      }  
  }

  C\#のrecord型は、値ベースの等価性比較と不変性を簡潔に実現するのに役立ちます 44。  
* 集約のテスト (NUnit, FluentAssertions):  
  C\#でも集約のテストは同様に、状態とイベントを検証します。  
  C\#  
  // Todo.cs (抜粋)  
  public class Todo : AggregateRoot  
  {  
      public DateTime? DeletedDateTime { get; private set; }  
      public void Delete()  
      {  
          DeletedDateTime \= DateTime.UtcNow;  
      }  
  }

  // TodoTests.cs

  public class TodoTests  
  {

      public void Delete\_should\_set\_deleted\_datetime()  
      {  
          // Arrange  
          var todo \= new Todo();

          // Act  
          todo.Delete();

          // Assert  
          todo.DeletedDateTime.Should().NotBeNull();  
      }  
  }

  この例では、DeleteメソッドがDeletedDateTimeプロパティを正しく設定することを検証しています 45。

### **6.3 Pythonでの実装例**

Pythonでは、pytestがデファクトスタンダードなテストフレームワークです。dataclassesやattrsライブラリが値オブジェクトの実装を助けます。

* **値オブジェクトと集約のテスト (pytest):**  
  Python  
  \# models.py  
  from dataclasses import dataclass

  @dataclass(frozen=True)  
  class Money:  
      amount: int  
      currency: str

  class Order:  
      def \_\_init\_\_(self, order\_id: str):  
          self.\_id \= order\_id  
          self.\_lines \=  
          self.\_total \= Money(0, "JPY")

      def add\_line(self, product\_id: str, price: Money):  
          self.\_lines.append((product\_id, price))  
          self.\_total \= Money(self.\_total.amount \+ price.amount, "JPY")

      @property  
      def total(self) \-\> Money:  
          return self.\_total

  \# tests/test\_models.py  
  def test\_adding\_line\_item\_updates\_total():  
      \# Arrange  
      order \= Order("order-123")  
      price1 \= Money(1000, "JPY")  
      price2 \= Money(500, "JPY")

      \# Act  
      order.add\_line("prod-1", price1)  
      order.add\_line("prod-2", price2)

      \# Assert  
      assert order.total \== Money(1500, "JPY")

  このテストは、Order集約に商品ラインを追加すると、合計金額が正しく更新されることを検証しています 47。  
* リポジトリのテスト (インメモリフェイク):  
  Pythonでは、依存関係を注入しやすい動的言語の特性を活かし、インメモリの辞書をバックエンドとするフェイクリポジトリを簡単に実装できます。  
  Python  
  \# repositories.py  
  class FakeOrderRepository:  
      def \_\_init\_\_(self):  
          self.\_orders \= {}

      def save(self, order: Order):  
          self.\_orders\[order.\_id\] \= order

      def get(self, order\_id: str) \-\> Order:  
          return self.\_orders.get(order\_id)

  \# tests/test\_services.py  
  def test\_place\_order\_use\_case():  
      \# Arrange  
      repo \= FakeOrderRepository()  
      service \= PlaceOrderService(repo) \# アプリケーションサービス

      \# Act  
      order\_id \= service.place\_order(...)  
      saved\_order \= repo.get(order\_id)

      \# Assert  
      assert saved\_order is not None  
      assert saved\_order.\_id \== order\_id

  このテストでは、アプリケーションサービスがフェイクリポジトリを介して注文を正しく保存することを確認しています 49。

## **第7章 陥穽を避ける：アンチパターンと戦略的提言**

ドメイン駆動設計（DDD）は強力なアプローチですが、その原則を誤解したり、不適切に適用したりすると、かえって複雑さを増大させる可能性があります。この章では、DDDのテスト戦略において頻繁に見られるアンチパターンを特定し、その根本原因を分析し、実践的な解決策を提示します。

### **7.1 原罪：貧血ドメインモデル（Anemic Domain Model）**

* 問題の定義：  
  貧血ドメインモデルとは、ドメインオブジェクトが単なるプロパティの入れ物（プロパティバッグ）となり、ゲッターとセッターを持つだけで、ビジネスロジックをほとんど含まない状態を指します。すべてのビジネスロジックは、ドメインオブジェクトを操作するサービス層のクラスに実装されてしまいます 50。これは、データと振る舞いを一体化させるというオブジェクト指向設計の基本理念に反する、DDDにおける「原罪」とも言えるアンチパターンです 52。  
* テストへの影響：  
  このアンチパターンは、DDDのテスト戦略を根底から覆します。ドメインオブジェクト自体に検証すべきビジネスロジックが存在しないため、ドメイン層の単体テストは無意味になります。結果として、すべてのテストはサービス層に対する統合テストとならざるを得ず、必然的に次項で述べる「The Mockery（嘲笑）」と呼ばれる過剰なモックの使用につながります。テストは複雑で脆くなり、DDDがもたらすはずの保守性や堅牢性といった利点は失われます 53。  
* 解決策：  
  解決策は、貧血モデルからリッチなドメインモデルへのリファクタリングです。サービス層に散らばったビジネスロジックを、関連するエンティティや値オブジェクトの内部に移動させます。公開セッターを廃止し、代わりにビジネスの振る舞いを表現する豊かなメソッド（例：order.addItem()）を公開し、コレクションをカプセル化することで、オブジェクトが自身の不変条件を維持できるようにします 55。

### **7.2 The Mockery：過剰または不適切なモック**

* 問題の定義：  
  内部的な協調オブジェクト、管理下の依存関係（自身のデータベースなど）、あるいは具象クラスを安易にモックしてしまう状態です。テストコードが、複雑なモックのセットアップと検証のコードで埋め尽くされてしまいます 35。  
* テストへの影響：  
  このようなテストは極めて脆く（brittle）、可読性が低く、実行も遅くなります。テストが検証しているのは、ビジネスの振る舞いではなく、コンポーネント間のやり取りという実装の詳細です。そのため、内部実装をリファクタリングするたびにテストが失敗し、開発の足かせとなります。これは、テストがパスしていてもシステムが正しく動作する保証がない「偽りの安心感」しか与えません 26。  
* 解決策：  
  第5章で詳述した「Khorikovの教義」を適用します。モックは、自分が所有していない外部依存関係（決済ゲートウェイなど）に対してのみ使用します。内部の協調オブジェクトや管理下の依存関係（データベースなど）に対しては、フェイク実装（インメモリリポジトリなど）や本物の実装を用いた統合テストを利用します。テストの焦点を、中間的なインタラクションの検証から、観測可能な最終結果の検証へとシフトさせることが重要です 27。

### **7.3 利口なUI（Smart UI）アンチパターン**

* 問題の定義：  
  ビジネスロジックが、UIコンポーネント（例：Webフレームワークのコントローラーやフロントエンドのコンポーネント）に直接埋め込まれてしまう状態です 58。  
* テストへの影響：  
  ビジネスロジックを、UIを起動せずにテストすることが不可能になります。これにより、テストは必然的に低速で不安定なE2Eテストに依存せざるを得なくなります。また、ロジックの再利用や組み合わせも困難になります。  
* 解決策：  
  レイヤードアーキテクチャの原則に従い、プレゼンテーションの関心事と、アプリケーションおよびドメインのロジックを明確に分離します 61。具体的には、「サービスの抽出（Extract Service）」や「エンティティの抽出（Extract Entity）」といったリファクタリング手法を適用し、UI層からビジネスロジックをアプリケーションサービスやドメインオブジェクトへと移動させます 60。

テストにおけるアンチパターンは、ほぼ常に、その背後にある設計上のアンチパターンの兆候です。テストスイートの保守に「痛み」を感じる時、それはコードベースがリファクタリングを求めている悲鳴に他なりません。したがって、テストスイートを改善するための主要なツールは、テストフレームワークやモッキングライブラリではなく、設計に関する知識とリファクタリングの技術です。  
この因果関係を理解することは、問題の根本解決にとって極めて重要です。例えば、「The Mockery」アンチパターンを考えてみましょう。直接的な問題は、テストにモックが多すぎることです。なぜモックが多いのでしょうか？それは、テスト対象のサービスが多すぎる依存関係を持っているからです。なぜサービスは多くの依存関係を持っているのでしょうか？それは、サービスが単一責任の原則に違反し、あまりにも多くのことをやろうとしているからです。なぜサービスは多くのことをやっているのでしょうか？それは、本来ドメインオブジェクトにあるべきビジネスロジックがサービスに漏れ出している、すなわち「貧血ドメインモデル」だからです。  
この 脆いテスト → 過剰なモック → 肥大化したサービス → 貧血ドメインモデル という因果の連鎖を逆に辿ることが、解決への道筋を示します。解決策は、より巧妙なモックの方法を見つけることではなく、ドメインモデルをリッチにすることです。それによりサービスは単純化し、依存関係が減り、過剰なモックの必要性がなくなり、結果としてシンプルで堅牢なテストが実現するのです。この洞察は、問題のあるテストスイートに直面したアーキテクトにとって、最も重要な指針となります。

## **第8章 結論：DDDにおける品質文化の醸成**

本レポートでは、ドメイン駆動設計（DDD）におけるテスト戦略を、その基本理念から具体的な実装パターン、そして避けるべきアンチパターンに至るまで、多角的に探求してきました。ここでの分析を通じて明らかになったのは、DDDにおけるテストが、単なる後工程の検証作業ではなく、設計そのものと分かちがたく結びついた、中心的かつ創造的な活動であるという事実です。  
堅牢なテスト戦略は、DDDにとってオプションの追加機能ではありません。それは、DDDが掲げる「複雑なドメインの管理」と「進化し続ける設計の実現」という約束を現実のものとするための必須メカニズムです。テストは、ドメインモデルの振る舞いを検証し、その正しさを保証することで、開発者が自信を持ってリファクタリングを行い、ドメインへの深い洞察をコードに反映させ続けることを可能にします。  
成功するDDDテスト戦略を構築するための核心的な原則は、以下の点に集約されます。

1. **振る舞いへの集中：** テストの焦点は、常に**ユビキタス言語**を通じて表現されるドメインモデルの**振る舞い**に置かれなければなりません。技術的な実装詳細ではなく、ビジネスルールが正しく機能するかを検証することが、価値あるテストの証です。  
2. **アーキテクチャの純粋性：** 特に依存関係の方向性を厳格に管理し、ドメイン層を外部の関心事から隔離することは、テスト可能なシステムを構築するための譲れない基盤です。このアーキテクチャ上の純粋性が、高速で信頼性の高い単体テストの土台を築きます。  
3. **目的に応じたテストタイプの選択：** テストピラミッドの原則に従い、ドメイン層の単体テストを厚くし、統合テストとE2Eテストを戦略的に配置することが、効率と網羅性のバランスを取る鍵です。また、「単体」か「統合」かというラベルに固執せず、「何を隔離し、何を検証するのか」という本質に基づいてテストを設計する洗練された視点が求められます。  
4. **設計へのフィードバックとしてのテスト：** テストの記述や保守に「痛み」を感じる場合、それはテスト技法の問題ではなく、根本的な設計上の問題を示唆する重要なシグナルです。貧血ドメインモデルや過剰なモックといったアンチパターンは、テストの観点から設計の改善点を浮き彫りにします。

最終的に、我々が目指すべきは、変更に対する強力なセーフティネットとして機能し、ビジネスドメインの正確かつ実行可能な仕様書としての役割を果たすテストスイートです。このようなテストスイートは、迅速なフィードバックを提供し、チーム全体に品質への自信をもたらします。DDDにおけるテスト戦略とは、単なる技法の集合ではなく、ドメインの本質を探求し、高品質なソフトウェアを継続的に提供するための文化そのものを醸成する活動なのです。

#### **引用文献**

1. ドメイン駆動設計完全に理解した \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/m10maeda/articles/i-had-domain-driven-design-down-pat](https://zenn.dev/m10maeda/articles/i-had-domain-driven-design-down-pat)  
2. Domain-Driven Design (DDD) \- GeeksforGeeks, 6月 17, 2025にアクセス、 [https://www.geeksforgeeks.org/system-design/domain-driven-design-ddd/](https://www.geeksforgeeks.org/system-design/domain-driven-design-ddd/)  
3. ふわっと理解するDDD \~ドメイン駆動設計\~ \- Qiita, 6月 17, 2025にアクセス、 [https://qiita.com/yu-saito-ceres/items/f73262cedcdd6e8e75c8](https://qiita.com/yu-saito-ceres/items/f73262cedcdd6e8e75c8)  
4. Laravel開発者のためのドメイン駆動設計（DDD）ガイド \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/rasshii/articles/9e21abaf7deccb](https://zenn.dev/rasshii/articles/9e21abaf7deccb)  
5. ドメイン駆動設計入門【DDDをわかりやすく解説】 \- 楽水, 6月 17, 2025にアクセス、 [https://rakusui.org/ddd/](https://rakusui.org/ddd/)  
6. 『ドメイン駆動設計をはじめよう』がわかりやすすぎた｜ミノ駆動 \- note, 6月 17, 2025にアクセス、 [https://note.com/minodriven/n/nbf9ac345b4da](https://note.com/minodriven/n/nbf9ac345b4da)  
7. DDD(ドメイン駆動設計)の魅力と実践：AIエージェント・戦略的設計から戦術的設計まで \- note, 6月 17, 2025にアクセス、 [https://note.com/tatsuyamatsuda/n/n671ce4e03d48](https://note.com/tatsuyamatsuda/n/n671ce4e03d48)  
8. Testing Strategies in Domain-Driven Design (DDD) \- DEV Community, 6月 17, 2025にアクセス、 [https://dev.to/ruben\_alapont/testing-strategies-in-domain-driven-design-ddd-2d93](https://dev.to/ruben_alapont/testing-strategies-in-domain-driven-design-ddd-2d93)  
9. You are naming your tests wrong\! \- Vladimir Khorikov, 6月 17, 2025にアクセス、 [https://khorikov.org/posts/2019-08-22-you-are-naming-your-tests-wrong/](https://khorikov.org/posts/2019-08-22-you-are-naming-your-tests-wrong/)  
10. DDDを実践するための手引き（概論・導入編）, 6月 17, 2025にアクセス、 [https://zenn.dev/kohii/articles/b96634b9a14897](https://zenn.dev/kohii/articles/b96634b9a14897)  
11. Best Practice \- An Introduction To Domain-Driven Design | Microsoft Learn, 6月 17, 2025にアクセス、 [https://learn.microsoft.com/en-us/archive/msdn-magazine/2009/february/best-practice-an-introduction-to-domain-driven-design](https://learn.microsoft.com/en-us/archive/msdn-magazine/2009/february/best-practice-an-introduction-to-domain-driven-design)  
12. 【React】ドメイン駆動設計への道（I）：理論編 \- Qiita, 6月 17, 2025にアクセス、 [https://qiita.com/Notta\_Engineering/items/4404ee7f3979c9cbf9d3](https://qiita.com/Notta_Engineering/items/4404ee7f3979c9cbf9d3)  
13. Spring+DDDでのアプリケーションアーキテクチャとテスト戦略 \- Qiita, 6月 17, 2025にアクセス、 [https://qiita.com/nyasba/items/66defc0b9d9786627cd8](https://qiita.com/nyasba/items/66defc0b9d9786627cd8)  
14. Blog: From Good To Great in DDD: Understanding the Suggested Architecture Pattern in Domain-Driven Design \- 7/10 \- Kranio, 6月 17, 2025にアクセス、 [https://www.kranio.io/en/blog/de-bueno-a-excelente-en-ddd-comprender-el-patron-de-arquitectura-sugerida-en-domain-driven-design---7-10](https://www.kranio.io/en/blog/de-bueno-a-excelente-en-ddd-comprender-el-patron-de-arquitectura-sugerida-en-domain-driven-design---7-10)  
15. Designing a DDD-oriented microservice \- .NET | Microsoft Learn, 6月 17, 2025にアクセス、 [https://learn.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/ddd-oriented-microservice](https://learn.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/ddd-oriented-microservice)  
16. Application layer vs domain layer? \- Software Engineering Stack Exchange, 6月 17, 2025にアクセス、 [https://softwareengineering.stackexchange.com/questions/140999/application-layer-vs-domain-layer](https://softwareengineering.stackexchange.com/questions/140999/application-layer-vs-domain-layer)  
17. DDD & Testing Strategy \- Lauri Taimila, 6月 17, 2025にアクセス、 [http://www.taimila.com/blog/ddd-and-testing-strategy/](http://www.taimila.com/blog/ddd-and-testing-strategy/)  
18. Domain Driven Design \- Layered Architecture \- uniknow.github.io, 6月 17, 2025にアクセス、 [https://uniknow.github.io/AgileDev/site/0.1.8-SNAPSHOT/parent/ddd/core/layered\_architecture.html](https://uniknow.github.io/AgileDev/site/0.1.8-SNAPSHOT/parent/ddd/core/layered_architecture.html)  
19. domain driven design \- DDD vs N-Tier (3-Tier) Architecture \- Stack Overflow, 6月 17, 2025にアクセス、 [https://stackoverflow.com/questions/3833920/ddd-vs-n-tier-3-tier-architecture](https://stackoverflow.com/questions/3833920/ddd-vs-n-tier-3-tier-architecture)  
20. The testing pyramid: Strategic software testing for Agile teams \- CircleCI, 6月 17, 2025にアクセス、 [https://circleci.com/blog/testing-pyramid/](https://circleci.com/blog/testing-pyramid/)  
21. Test Pyramid \- Martin Fowler, 6月 17, 2025にアクセス、 [https://martinfowler.com/bliki/TestPyramid.html](https://martinfowler.com/bliki/TestPyramid.html)  
22. Testing Pyramid a Simple Way to Ensure Code Quality \- Samurai Developer, 6月 17, 2025にアクセス、 [https://samurai-developer.com/testing-pyramid/](https://samurai-developer.com/testing-pyramid/)  
23. 「ドメイン駆動設計をはじめよう」を読んだので紹介します | 豆蔵デベロッパーサイト, 6月 17, 2025にアクセス、 [https://developer.mamezou-tech.com/blogs/2024/08/15/book-learning-domain-driven-design/](https://developer.mamezou-tech.com/blogs/2024/08/15/book-learning-domain-driven-design/)  
24. ビジネスの複雑性を捉えるためのDDDアプローチ \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/currypun/articles/4559318851dfbe](https://zenn.dev/currypun/articles/4559318851dfbe)  
25. 実装パターンとテストパターンの紹介と組み合わせ方 \- Speaker Deck, 6月 17, 2025にアクセス、 [https://speakerdeck.com/suzukimar/shi-zhuang-patantotesutopatannoshao-jie-tozu-mihe-wasefang](https://speakerdeck.com/suzukimar/shi-zhuang-patantotesutopatannoshao-jie-tozu-mihe-wasefang)  
26. Mock all the things? \- Matthijs Wagemakers, 6月 17, 2025にアクセス、 [https://www.wagemakers.net/posts/mock-all-the-things/](https://www.wagemakers.net/posts/mock-all-the-things/)  
27. When to Mock · Enterprise Craftsmanship, 6月 17, 2025にアクセス、 [https://enterprisecraftsmanship.com/posts/when-to-mock/](https://enterprisecraftsmanship.com/posts/when-to-mock/)  
28. Using Value Objects to represent technical concerns \- Enterprise Craftsmanship, 6月 17, 2025にアクセス、 [https://enterprisecraftsmanship.com/posts/using-value-objects-represent-technical-concerns/](https://enterprisecraftsmanship.com/posts/using-value-objects-represent-technical-concerns/)  
29. Value Objects in Depth in Domain-Driven Design \- DEV Community, 6月 17, 2025にアクセス、 [https://dev.to/ruben\_alapont/value-objects-in-depth-in-domain-driven-design-2cbj](https://dev.to/ruben_alapont/value-objects-in-depth-in-domain-driven-design-2cbj)  
30. ValueObject について、複数の書籍を参考にして学習しました \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/rescuenow/articles/560913342448f7](https://zenn.dev/rescuenow/articles/560913342448f7)  
31. DDD Part 2: Tactical Domain-Driven Design | Vaadin, 6月 17, 2025にアクセス、 [https://vaadin.com/blog/ddd-part-2-tactical-domain-driven-design](https://vaadin.com/blog/ddd-part-2-tactical-domain-driven-design)  
32. Unit Tests \- Should I have unit tests for the Entity/Value Object level or just at the Aggregate Root level? \- Stack Overflow, 6月 17, 2025にアクセス、 [https://stackoverflow.com/questions/60749027/unit-tests-should-i-have-unit-tests-for-the-entity-value-object-level-or-just](https://stackoverflow.com/questions/60749027/unit-tests-should-i-have-unit-tests-for-the-entity-value-object-level-or-just)  
33. curated-resources-for-domain-driven-design/blog/0004-how-to ..., 6月 17, 2025にアクセス、 [https://github.com/SAP/curated-resources-for-domain-driven-design/blob/main/blog/0004-how-to-develop-aggregates.md](https://github.com/SAP/curated-resources-for-domain-driven-design/blob/main/blog/0004-how-to-develop-aggregates.md)  
34. Blog: From Good To Great in DDD: Understanding the Domain-Services Patterns in Domain-Driven Design \- 5/10 \- Kranio, 6月 17, 2025にアクセス、 [https://www.kranio.io/en/blog/de-bueno-a-excelente-en-ddd-comprender-los-patrones-de-domain-services-en-domain-driven-design---5-10](https://www.kranio.io/en/blog/de-bueno-a-excelente-en-ddd-comprender-los-patrones-de-domain-services-en-domain-driven-design---5-10)  
35. testing \- How to avoid too much mocking in unit tests in a database ..., 6月 17, 2025にアクセス、 [https://softwareengineering.stackexchange.com/questions/454887/how-to-avoid-too-much-mocking-in-unit-tests-in-a-database-heavy-method](https://softwareengineering.stackexchange.com/questions/454887/how-to-avoid-too-much-mocking-in-unit-tests-in-a-database-heavy-method)  
36. Understanding stubs, fakes and mocks. \- unit testing \- Stack Overflow, 6月 17, 2025にアクセス、 [https://stackoverflow.com/questions/14891967/understanding-stubs-fakes-and-mocks](https://stackoverflow.com/questions/14891967/understanding-stubs-fakes-and-mocks)  
37. What is a Test Double? | Types & Best Practices \- Testsigma, 6月 17, 2025にアクセス、 [https://testsigma.com/blog/test-doubles/](https://testsigma.com/blog/test-doubles/)  
38. The definitive guide to test doubles on Android — Part 1: Theory \- droidcon, 6月 17, 2025にアクセス、 [https://www.droidcon.com/2022/05/29/the-definitive-guide-to-test-doubles-on-android-part-1-theory/](https://www.droidcon.com/2022/05/29/the-definitive-guide-to-test-doubles-on-android-part-1-theory/)  
39. What's the difference between faking, mocking, and stubbing? \- Stack Overflow, 6月 17, 2025にアクセス、 [https://stackoverflow.com/questions/346372/whats-the-difference-between-faking-mocking-and-stubbing](https://stackoverflow.com/questions/346372/whats-the-difference-between-faking-mocking-and-stubbing)  
40. How to assess the value of a unit test: The four pillars of an ideal test \- Talon.One, 6月 17, 2025にアクセス、 [https://www.talon.one/blog/how-to-asses-the-value-of-a-unit-test](https://www.talon.one/blog/how-to-asses-the-value-of-a-unit-test)  
41. DomingoAlvarez99/ddd-example: Example of Hexagonal ... \- GitHub, 6月 17, 2025にアクセス、 [https://github.com/DomingoAlvarez99/ddd-example](https://github.com/DomingoAlvarez99/ddd-example)  
42. 単純すぎるドメイン駆動設計Javaサンプルコード (4) テスト \#DDD \- Qiita, 6月 17, 2025にアクセス、 [https://qiita.com/ynstkt/items/6d390a6e0a4248905af5](https://qiita.com/ynstkt/items/6d390a6e0a4248905af5)  
43. DDD Bounded Contexts and Java Modules \- Baeldung, 6月 17, 2025にアクセス、 [https://www.baeldung.com/java-modules-ddd-bounded-contexts](https://www.baeldung.com/java-modules-ddd-bounded-contexts)  
44. ドメイン駆動設計(DDD)を整理 \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/sutamac/articles/7e864fb9e30d70](https://zenn.dev/sutamac/articles/7e864fb9e30d70)  
45. とにかくドメイン駆動設計を実践してみる試み ～TODO管理システム編～ \- Zenn, 6月 17, 2025にアクセス、 [https://zenn.dev/tatsuteb/articles/f2d05abb8ce9a6](https://zenn.dev/tatsuteb/articles/f2d05abb8ce9a6)  
46. With code examples, learn how to develop ASP.NET Core C\# 12 using domain-driven design, 6月 17, 2025にアクセス、 [https://www.c-sharpcorner.com/article/with-code-examples-learn-how-to-develop-asp-net-core-c-sharp-12-using-domain-driven/](https://www.c-sharpcorner.com/article/with-code-examples-learn-how-to-develop-asp-net-core-c-sharp-12-using-domain-driven/)  
47. ddd-for-python·PyPI, 6月 17, 2025にアクセス、 [https://pypi.org/project/ddd-for-python/](https://pypi.org/project/ddd-for-python/)  
48. Crafting Maintainable Python Applications with Domain-Driven Design and Clean Architecture \- ThinhDA, 6月 17, 2025にアクセス、 [https://thinhdanggroup.github.io/python-code-structure/](https://thinhdanggroup.github.io/python-code-structure/)  
49. jagoPG/py-ddd: A simple example of how to implement DDD under a hexagonal architecture. \- GitHub, 6月 17, 2025にアクセス、 [https://github.com/jagoPG/py-ddd](https://github.com/jagoPG/py-ddd)  
50. Anaemic Domain Model vs. Rich Domain Model | Ensono, 6月 17, 2025にアクセス、 [https://www.ensono.com/insights-and-news/expert-opinions/anaemic-domain-model-vs-rich-domain-model/](https://www.ensono.com/insights-and-news/expert-opinions/anaemic-domain-model-vs-rich-domain-model/)  
51. Anemic domain model \- Wikipedia, 6月 17, 2025にアクセス、 [https://en.wikipedia.org/wiki/Anemic\_domain\_model](https://en.wikipedia.org/wiki/Anemic_domain_model)  
52. Anemic Domain Model \- Martin Fowler, 6月 17, 2025にアクセス、 [https://martinfowler.com/bliki/AnemicDomainModel.html](https://martinfowler.com/bliki/AnemicDomainModel.html)  
53. 3 ways to avoid an anemic domain model in EF Core \- DevTrends, 6月 17, 2025にアクセス、 [https://www.devtrends.co.uk/blog/3-ways-to-avoid-an-anemic-domain-model-in-ef-core](https://www.devtrends.co.uk/blog/3-ways-to-avoid-an-anemic-domain-model-in-ef-core)  
54. Why anemic models are considered as anti-pattern and how to replace it? : r/dotnet \- Reddit, 6月 17, 2025にアクセス、 [https://www.reddit.com/r/dotnet/comments/1evlvxq/why\_anemic\_models\_are\_considered\_as\_antipattern/](https://www.reddit.com/r/dotnet/comments/1evlvxq/why_anemic_models_are_considered_as_antipattern/)  
55. Anemic vs. Rich Domain Objects | Baeldung, 6月 17, 2025にアクセス、 [https://www.baeldung.com/java-anemic-vs-rich-domain-objects](https://www.baeldung.com/java-anemic-vs-rich-domain-objects)  
56. Home · Ozius Solutions, 6月 17, 2025にアクセス、 [https://www.ozius.solutions/blog/rich-domain-vs-anemic-domain-comparison/](https://www.ozius.solutions/blog/rich-domain-vs-anemic-domain-comparison/)  
57. Unit testing Anti-patterns catalogue \- tdd \- Stack Overflow, 6月 17, 2025にアクセス、 [https://stackoverflow.com/questions/333682/unit-testing-anti-patterns-catalogue](https://stackoverflow.com/questions/333682/unit-testing-anti-patterns-catalogue)  
58. DDD難民に捧げる Domain-Driven Designのエッセンス 第2回 DDDの基礎と実践, 6月 17, 2025にアクセス、 [https://www.ogis-ri.co.jp/otc/hiroba/technical/DDDEssence/chap2.html](https://www.ogis-ri.co.jp/otc/hiroba/technical/DDDEssence/chap2.html)  
59. フロントエンドの複雑さに立ち向かう 〜DDDとClean Architectureを携えて〜 | さくらのナレッジ, 6月 17, 2025にアクセス、 [https://knowledge.sakura.ad.jp/36776/](https://knowledge.sakura.ad.jp/36776/)  
60. Extract Service from Smart UI — Henning Schwentner, 6月 17, 2025にアクセス、 [https://hschwentner.io/domain-driven-refactorings/tactical/extract-service-from-smart-ui.html](https://hschwentner.io/domain-driven-refactorings/tactical/extract-service-from-smart-ui.html)  
61. Domain-Driven Design \- Andriy Buday, 6月 17, 2025にアクセス、 [https://andriybuday.com/2010/01/ddd.html](https://andriybuday.com/2010/01/ddd.html)