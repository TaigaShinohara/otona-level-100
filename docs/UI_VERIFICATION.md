# 実機なし検証（交付前チェック）

AGENTS.md「Pre-delivery UI verification standard」（2026-11-23、本人の決定）の
このプロジェクトでの実装記録。今後の交付では、ビルド・単体テストと並べて
この検証を必ず回す。

## 構成

- **画面テスト**: `app/src/test/java/com/taigashinohara/otonalevel100/UiVerificationTest.kt`
  - Robolectric 4.14.1（`@GraphicsMode(NATIVE)`、SDK 35）で MainActivity を実起動し、
    実際のボタンを押して画面遷移させる。
  - Roborazzi 1.35.0 の `captureRoboImage(file)` でスクリーンショットを記録し、
    併せて素の PNG を `app/build/outputs/ui-check/` にも出す（目視確認用）。
  - レイアウト崩壊ガード: 主要ラベルが「表示されている＋高さ > 0」であることを
    各画面で assert する（高さ0で真っ白になる失敗形の検出）。
- **実行**: `./run_tests.sh` が正本（ロジック16件＋UI 4件＝全20件を JUnitCore で直接実行）。
  - Gradle の `testDebugUnitTest` はこのVMでは使えない（テスト実行プロセスの
    結果返送チャネル（ループバック）が干渉され、TestResultProcessor の NPE で
    実行プロセスが死ぬ。テスト自体の問題ではない）。
  - 実行時の工夫（run_tests.sh 内にコメントあり）:
    - AGP生成の `test_config.properties` のディレクトリを classpath に含める。
    - SDK のスタブ `android.jar` を classpath 末尾に置く（@Config のデフォルト値が
      参照する android.app.Application をシステム側でも解決できるように）。
    - `.aar` のままでは JVM から読めないため、各 AAR の `classes.jar` を
      `app/build/aar-jars/` に展開して classpath に足す。
    - Robolectric の android-all は Java の TLS がこのVMで通らないため実行時に
      落とせず、`.local/robolectric-deps/` に事前取得した
      `android-all-instrumented-15-robolectric-12650502-i7.jar` を
      `robolectric.offline=true` + `robolectric.dependency.dir` で使う。
    - テスト JVM の作業ディレクトリは app モジュール直下（Gradle のテスト実行と
      同条件。test_config.properties の相対パス解決のため）。
    - RoborazziRule + LastImage は Compose 前提でプレーンな View 階層では
      例外になるため、ルールは使わず file 形式の capture を使う。
- **ビルド直列化**: `build.sh` は `~/workspace/tools/with-build-lock.sh` 経由で
  Gradle を実行する（AGENTS.md「Build serialization rule」）。あわせて
  GRADLE_USER_HOME はプロジェクト内 `.local/gradle-home` を使用する
  （共有ホームの journal ロックは、ラッパー未適用の他ビルドとまだ競合するため）。

## カバー範囲（最低ラインとの対応）

- 起動直後の画面: ホーム（`home_firstScreen_renders`）
- オンボーディング入口: **対象外** — このアプリにオンボーディングは未実装
  （ストア提出前の実装予定項目。実装した版で入口画面の撮影を追加する）
- その版で作り替えた画面: v0.9.0 で直した問題画面（`question`）とタブ遷移
  - `session_tabsStayEnabled_andSessionSurvivesTabLeave`: セッション中に
    全タブが有効なこと（v0.9.0 の修正の回帰テスト）、タブで離れても
    セッションが保持されホームに「つづきから」が出ること、再開で問題に
    戻れることを検証。カテゴリ画面も撮影。
  - `reviewScreen_renders`: 復習画面の描画と件数チップ。
  - `fullSession_reachesResult`: 5問を通しで解いて結果画面まで到達。

## 初回結果（2026-11-23）

- 全20件合格（ロジック16＋UI 4）。スクリーンショット5枚
  （home / question / categories / review / result）を目視確認し、
  崩壊・見切れ・真っ白はなし。
