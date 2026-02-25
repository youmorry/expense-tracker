# Java Coding Standards

[フューチャー Java コーディング規約](https://future-architect.github.io/coding-standards/documents/forJava/Javaコーディング規約.html) に準拠する。**インデントは半角スペース 2 文字**（規約原文の 4 文字から変更）。

以下は規約の要点。迷った場合は原文を参照すること。

## Naming

- パッケージ: すべて小文字、意味のある名前
- クラス / インターフェース / Enum / Record: UpperCamelCase（例: `TransactionService`）
- メソッド / 変数: lowerCamelCase（例: `getName()`）
- 定数: `UPPER_SNAKE_CASE`（`static final` で定義）
- boolean 変数・メソッド: 状態が明確な名前（`isOpen`, `hasError`）
- getter: `get+属性名`（boolean は `is+属性名`）、setter: `set+属性名`
- 変換メソッド: `to+オブジェクト名`（例: `toDto()`）
- メソッド引数とインスタンス変数を同名にしない
- 大文字・小文字の違いだけで名前を区別しない

## Formatting

- インデント: **半角スペース 2 文字**
- 1 行に 1 ステートメント
- `{` の後に同行でステートメントを書かない
- 制御文（`if`, `for`, `while` 等）の `{}` は省略禁止
- 空の `{}` ブロック禁止
- カンマの後に空白
- 代入演算子・ビット演算子・論理演算子・関係演算子・算術演算子の前後に空白
- `++` `--` とオペランドの間に空白を入れない
- `for` 文内のセミコロン後に空白
- `return` 文で不要なカッコを付けない
- `== true` / `== false` との比較をしない
- 不等号は左向き（`<`, `<=`）に統一

## Imports

- `java.lang` パッケージの import は不要
- ワイルドカード `*` 使用禁止
- static import は原則禁止

## Variables & Constants

- 1 ステートメントにつき 1 変数宣言
- マジックナンバー禁止（定数化する）
- 配列宣言は `型名[]` 形式（`String[] args`）
- `public` フィールドは定数のみ
- `var` は右辺で型が明確な場合のみ使用

## Control Structures

- `if` / `while` の条件式で `=`（代入）を使わない
- `for` と `while` を意識的に使い分ける（回数既知は `for`）
- `for` ループカウンタは 0 始まり、ループ内で変更しない
- 配列・コレクション全要素のループは拡張 for 文を使用
- ループ内のオブジェクト生成は最小限に
- ループ内の try ブロックは原則ループ外に出す
- 条件分岐が多い場合はオブジェクト指向的な設計で対応
- switch 式（Java 14+）を推奨

## String & Numeric

- 文字列比較は `equals()` メソッド（`==` 禁止）
- 文字列リテラルを `new String()` しない
- 繰り返し文字列連結は `StringBuilder` を使用（1 ステートメント内は `+` で可）
- リテラル側で `equals()` を呼ぶ（`"value".equals(var)` で NPE 回避）
- 誤差なき計算は `BigDecimal` を使用
- `BigDecimal` の文字列化は `toPlainString()`

## Object-Oriented

- `@Override` アノテーション必須
- オブジェクト比較は `equals()` を使用（`==` はプリミティブのみ）
- `instanceof` の型キャストはパターンマッチングを使用
- インターフェース型での参照を推奨（例: `List<>` で宣言、`ArrayList<>` で生成）
- `@Deprecated` API は使用しない
- 適切なアクセス修飾子を選択（必要最小限の公開範囲）
- `final` を適切に利用

## Stream API & Lambda

- Stream API 利用可、ただし並列ストリーム（`parallelStream`）は禁止
- Stream のメソッドチェーン改行はピリオド前
- 中間処理は 3 行程度を目安
- ラムダ式はメソッド参照が使える場合はメソッド参照を優先
- ラムダ式の型宣言は省略
- ラムダ式は 1 行に収める

## Exception Handling

- catch は詳細な例外クラスで受ける（`Exception` で包括的に catch しない）
- `Exception` / `RuntimeException` を直接 throw しない（具体的な例外クラスを使用）
- catch ブロックで処理を空にしない（意図的に無視する場合は `// ignore` コメント）
- リソース管理は try-with-resources 文を使用
- `finalize()` をオーバーライドしない

## Javadoc

- `@param`, `@return`, `@throws` を記述する
- サンプルコードは `{@snippet}` タグを使用
