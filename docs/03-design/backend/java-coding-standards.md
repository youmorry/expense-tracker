# Java Coding Standards

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) に準拠する。

フォーマッターは [google-java-format](https://github.com/google/google-java-format)、静的解析は [Checkstyle](https://checkstyle.org/)（`google_checks.xml` ベース）を使用する。

## プロジェクト固有の補足

- Javadoc: `public`メソッドには説明、`@param`, `@return`, `@throws`を記述する。ただし Record の自動生成アクセサや自明な getter/setter は省略可

## `var`（ローカル変数型推論）の利用規約

基本原則: **「変数宣言の行だけを読んで型がわかるか？」— わからなければ `var` を使わない。**

### 使ってよい場面

1. **右辺から型が明白な場合（`new` によるインスタンス生成）**

```java
var user = new User("Alice");              // User であることが明白
var list = new ArrayList<String>();         // ArrayList<String> が明白
```

2. **ファクトリメソッド・`of` 系で型が明白な場合**

```java
var ids = List.of(1, 2, 3);               // List<Integer>
var map = Map.of("key", "value");          // Map<String, String>
```

3. **try-with-resources のローカル変数**

```java
try (var reader = new BufferedReader(new FileReader(path))) { ... }
```

### 使うべきでない場面

1. **右辺だけでは型が推測困難な場合（メソッド呼び出しの戻り値）**

```java
// NG: 戻り値の型が不明
var result = service.findResult(query);
// OK
ExpenseReport result = service.findResult(query);
```

2. **数値リテラルで意図する型が曖昧な場合**

```java
// NG: int? long? double?
var amount = 0;
// OK
long amount = 0;
```

3. **ダイヤモンド演算子 `<>` との併用で型引数が消える場合**

```java
// NG: 要素型が不明
var list = new ArrayList<>();
// OK
var list = new ArrayList<String>();
```

4. **三項演算子やチェーンで戻り値型が非自明な場合**

```java
// NG
var value = flag ? getA() : getB();
```
