# バリデーション方針

## 概要

Expense Tracker における入力バリデーションとドメインルールのバリデーションの実装方針を定義する。
バリデーションを3つの層に分離し、それぞれの責務・例外・HTTP ステータスを明確にする。

---

## バリデーションの3層構造

| 層 | 責務 | 例外 | HTTP | 備考 |
|---|---|---|---|---|
| Presentation | 形式的な入力チェック | `MethodArgumentNotValidException` | 422 | Bean Validation 標準アノテーションのみ |
| Application | ユースケース固有のビジネスルール | `ValidationException` | 422 | ドメインオブジェクト生成**前**に検証 |
| Domain | オブジェクトの不変条件 | `IllegalArgumentException` | 500 | 違反はバグ扱い |

---

## Presentation 層: 入力バリデーション

### 責務

リクエストの形式的な正しさを検証する。ビジネス上の意味は問わない。

### 手段

- **Bean Validation**: `@NotNull`, `@Positive`, `@Size` 等の標準アノテーションを使用する
- **Jackson デシリアライズ**: 型変換（例: `"abc"` → `BigDecimal`）はデシリアライズ時に検証される
- カスタムバリデーター（`@Constraint`）は原則作成しない

### Bean Validation とドメイン不変条件の重複

Bean Validation とドメインオブジェクトのコンストラクタで**同じチェックを重複して行う**。

```java
// Presentation 層: DTO に Bean Validation アノテーション
public record CreateTransactionRequest(
    @NotNull LocalDate date,
    @NotNull BigDecimal amount,
    // ...
) {}

// Domain 層: コンストラクタで不変条件を検証
public record Money(BigDecimal value) {
    public Money {
        Objects.requireNonNull(value, "value must not be null");
    }
}
```

Bean Validation が前段でガードするため、正常なリクエストフローではドメインの不変条件違反は発生しない。
ドメイン側の検証は防御的プログラミングとして存在し、違反時はバグとして扱う（500 エラー）。

### 実行順序とエラーレスポンス

リクエスト処理は以下の順序で実行される。前段で失敗した場合、後段のバリデーションは実行されない。

```
Jackson デシリアライズ（400）→ Bean Validation（422）→ ビジネスバリデーション（422）→ ドメイン生成
```

| 段階 | 失敗時の例外 | HTTP | 備考 |
|---|---|---|---|
| Jackson デシリアライズ | `HttpMessageNotReadableException` | 400 | フィールドエラー一覧は返らない |
| Bean Validation | `MethodArgumentNotValidException` | 422 | `errors` 配列でフィールドエラーを返す |

例えば `"amount": "abc"` のようなリクエストでは 400 のみが返り、他のフィールドに Bean Validation エラーがあっても 422 のフィールドエラー一覧は返らない。FE 側では 400 と 422 を区別して処理する。

---

## Application 層: ビジネスバリデーション

### 責務

DB 問い合わせが必要なルールや、ユースケース固有のビジネスルールを検証する。
ドメインオブジェクト生成**前**に検証し、`ValidationException` をスローする。

### 対象となるルールの例

- カテゴリ ID の存在チェック（DB 問い合わせが必要）
- ユースケース固有の制約（特定の操作でのみ適用されるルール）

### コード例

```java
public TransactionResult create(TransactionCreateCommand cmd) {
    // ビジネスバリデーション — ドメイン生成前に検証
    Category category = categoryRepository.findById(cmd.categoryId())
        .orElseThrow(() -> new ValidationException(
            List.of(new FieldError("category not found", "categoryId"))));

    // ドメインオブジェクト生成 — ここでの IAE はバグ（catch しない → 500）
    var money = new Money(cmd.amount());
    var transaction = new Transaction(...);

    return transactionRepository.save(transaction);
}
```

### Domain の `IllegalArgumentException` を catch しない

Application 層では、ドメインオブジェクトの `IllegalArgumentException` を意図的に catch しない。
Bean Validation を通過した後にドメインの不変条件に違反する値が到達するのはプログラミングエラーであり、
500 として `GlobalExceptionHandler` に処理を委ねる。

### ユースケース横断のビジネスルールの重複

複数のユースケースで同じビジネスルール検証が必要になる場合がある（例: カテゴリ ID の存在チェックが create と update の両方で必要）。

**現時点の方針: 同一 Service クラス内の private メソッドに抽出する。**

```java
// 同一 Service 内で共通化
private Category findCategoryOrThrow(CategoryId categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ValidationException(
            List.of(new FieldError("category not found", "categoryId"))));
}
```

複数の Service クラスにまたがって同じビジネスルールが必要になった場合は、ドメインサービスへの切り出しを検討する。
ただし、現時点ではその規模のルールは存在しないため、ドメインサービスや新たな例外階層は導入しない。

---

## Domain 層: 不変条件

### 責務

ドメインオブジェクトが常に満たすべき不変条件をコンストラクタで検証する。
不正な状態のオブジェクトが生成されることを防ぐ。

### 例外

`IllegalArgumentException` をスローする。これはバグ扱いであり、`GlobalExceptionHandler` で 500 として処理される。
`AppException` のサブクラスは使用しない。

### ドメイン不変条件とユースケース固有ルールの区別

| 種類 | 定義 | 検証場所 | 例 |
|---|---|---|---|
| ドメイン不変条件 | オブジェクトが常に満たすべき条件 | Domain 層（コンストラクタ） | Money が null でないこと |
| ユースケース固有ルール | 特定の操作でのみ適用される制約 | Application 層（Service） | カテゴリ ID の存在チェック |

**判断基準**: 「そのオブジェクトが存在する限り常に成り立つべきか？」を問う。
常に成り立つべきならドメイン不変条件、特定の文脈でのみ必要ならユースケース固有ルール。

---

## Command / Query オブジェクト

Application 層の入力オブジェクト（`TransactionCreateCommand` 等）にはバリデーションを持たせない。
Presentation 層の Bean Validation で検証済みの値を運ぶ**入れ物**として扱う。

ただし、必須フィールドに対する `Objects.requireNonNull` は防御的プログラミングとして許容する。
これはバリデーションではなく、プログラミングエラーの早期検出を目的とする。

```java
// バリデーションなし — 検証済みの値を保持するだけ
// requireNonNull は防御的プログラミングとして許容
public record TransactionCreateCommand(
    UserId userId,
    LocalDate date,
    BigDecimal amount,
    CategoryId categoryId,
    NeedWantType needWantType,
    String title,
    String memo
) {
    public TransactionCreateCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
```

---

## バリデーション一覧

各フィールドのバリデーションルール・エラーメッセージ・pointer の一覧は[エラーハンドリング設計](../common/error-handling.md)のエラー種別カタログを参照。
