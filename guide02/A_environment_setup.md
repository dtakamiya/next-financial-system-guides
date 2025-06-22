# 付録A：開発環境の構築

このガイドで紹介するコードを実際に動かすために必要な開発環境の構築手順です。

## 必要なツール

-   **Java Development Kit (JDK)**: Java 17
    -   [Amazon Corretto](https://aws.amazon.com/corretto/) や [Eclipse Temurin](https://adoptium.net/) などのディストリビューションを推奨します。
    -   `SDKMAN!` を使うとバージョンの切り替えが簡単になり便利です。
        ```bash
        sdk install java 17.0.10-tem
        ```

-   **Maven**: Javaのビルドツールおよび依存関係管理ツール。
    -   Spring Bootプロジェクトの多くで標準的に使われています。
    -   `./mvnw` (Maven Wrapper) がプロジェクトに含まれているため、個別のインストールは必須ではありません。

-   **Docker Desktop**: Testcontainersやローカルでの動作確認に必要。
    -   [公式サイト](https://www.docker.com/products/docker-desktop/) からダウンロードしてインストールしてください。
    -   バックグラウンドで起動していることを確認してください。

-   **IDE (統合開発環境)**: コーディングを効率化するエディタ。
    -   **IntelliJ IDEA Community Edition** (無料) または Ultimate Edition (有料) を推奨します。
        -   Groovy (Spock) や Spring Boot のサポートが手厚いです。
    -   **Visual Studio Code** と Java拡張機能パックの組み合わせも良い選択肢です。

-   **HTTPクライアント**: APIの動作確認に使用。
    -   `curl`: コマンドラインで使える定番ツール。
    -   [Postman](https://www.postman.com/) や [Insomnia](https://insomnia.rest/): GUIでリクエストを管理できる高機能なツール。
    -   IntelliJ IDEA Ultimate Editionには、高機能なHTTPクライアントが組み込まれています。

## IntelliJ IDEAでのセットアップ（推奨）

1.  **プロジェクトを開く**: IntelliJ IDEAを起動し、「Open」から `pom.xml` ファイルを含むプロジェクトのルートディレクトリを選択します。
2.  **JDKの確認**: `File > Project Structure > Project` で、Project SDKが `17` に設定されていることを確認します。
3.  **Mavenの同期**: 初回読み込み時に、Mavenの依存関係が自動的にダウンロードされます。右側のMavenタブから手動で同期することも可能です。
4.  **プラグイン**: `File > Settings > Plugins` から以下のプラグインがインストールされていることを確認します。（通常はバンドルされています）
    -   Groovy
    -   MyBatisX (MyBatis MapperとJavaコード間のジャンプが便利になります)

## Spockテストの実行

Spockのテストは `.groovy` という拡張子のファイルに記述します。
IntelliJ IDEAでは、テストクラスやメソッドの横にある緑色の再生ボタンをクリックするだけで、簡単にテストを実行できます。

Mavenコマンドで全てのテストを実行する場合は、プロジェクトのルートディレクトリで以下のコマンドを実行します。
```bash
./mvnw clean test
```
これにより、コンパイル、単体テスト、結合テスト（TestcontainersによるDB起動を含む）がすべて実行されます。 