# Backend AGENTS.md

Backend (Spring Boot / Java) のコード実装時に適用するルール。プロジェクト全体の不変原則は `../docs/00-constitution.md` を参照する。

## アーキテクチャ: DDD + Layered

```
com.youmorry.expensetracker/
├── domain/           # Entity, ValueObject, Repository interface
├── application/      # Service（ユースケース実装）
├── infrastructure/   # Spring Data JDBC Repository 実装, 外部 API
├── presentation/     # REST Controller, DTO
└── shared/           # 横断的関心事（例外クラス等）
```

- `domain` と `application` を明示的に分離し、ドメインロジックを `domain` に集約
- Spring Data JDBC の Aggregate/Repository 概念を DDD にそのまま対応させる
- `domain` 層はフレームワークのロジックに依存しない（`@Table`, `@Id` 等のマッピング用アノテーションは許容）

### 層間の依存ルール

- 依存方向: presentation → application → domain ← infrastructure
- domain 層は他の層に依存しない（依存性逆転）
- Controller から Repository を直接呼ばない（必ず application 層を経由）

### インターフェースの配置規約

- **Repository インターフェース** → `domain/` に配置する（リポジトリはドメイン概念のため）
- **それ以外のインフラ抽象**（認証ポート等） → `application/` の該当ユースケースパッケージに配置する
- ヘキサゴナルアーキテクチャの `port` パッケージは使用しない（レイヤードアーキテクチャとの整合性を保つため）

### application 層の設計指針

- CQRS（Command Query Responsibility Segregation）を採用する
  - 書き込み系の入力: `<Entity>CreateCommand`, `<Entity>UpdateCommand` など
  - 読み取り系の入力: `<Entity>SearchQuery`, `<Entity>GetQuery` など
  - 出力: `<Entity>Result`
- 同値ガード（変更がない場合に save をスキップ）は、外部 API 呼び出しや重い処理を伴う場合にのみ導入する
- 単純な DB 保存のみの場合は条件分岐を入れず、冪等性をシンプルに保つ

### 禁止事項

- domain 層で Spring 固有のサービス（`@Service`, `@Transactional` 等）を使わない
- Controller にビジネスロジックを書かない
- Entity のコンストラクタで不変条件を検証せずにインスタンスを作らない

### コード例

#### 良い例: Entity でバリデーション付きコンストラクタ

```java
public record Money(BigDecimal value) {

  public Money {
    Objects.requireNonNull(value, "value must not be null");
  }
}
```

#### 悪い例: バリデーションなしの Entity

```java
// NG: 不変条件を検証していない。不正な状態のオブジェクトが生成される
public record Money(BigDecimal value) {}
```

#### 良い例: Controller → Service の呼び出し

```java
// Controller: 薄く保ち、ビジネスロジックを書かない
@PostMapping
public ResponseEntity<TransactionResponse> create(
    @Valid @RequestBody CreateTransactionRequest request,
    @AuthenticationPrincipal UserId userId) {
  var transaction = transactionService.create(request.toCommand(userId));
  return ResponseEntity.status(HttpStatus.CREATED)
      .body(TransactionResponse.from(transaction));
}
```

#### 悪い例: Controller にビジネスロジック

```java
// NG: Controller 内でドメインロジックを実行している
@PostMapping
public ResponseEntity<TransactionResponse> create(
    @Valid @RequestBody CreateTransactionRequest request,
    @AuthenticationPrincipal UserId userId) {
  var category = categoryRepository.findById(request.categoryId())
      .orElseThrow(() -> new ValidationException(...));
  var money = new Money(new BigDecimal(request.amount()));
  // ... 本来 Service に属するロジック
}
```

## 設計判断

- **Spring Data JDBC over JPA**: 暗黙の挙動（遅延ロード・ダーティチェック）を排除し SQL を透明に
- **HS256 (symmetric JWT)**: 単一サーバー構成のためシンプルな対称鍵で十分

## API Conventions

- `/api/v1/` prefix、URL リソース名は複数形
- JSON キー・クエリパラメータは snake_case
- 金額は String 型（浮動小数点誤差回避）
- null の場合はキーを省略（null ではなく undefined）
- エラーレスポンスは RFC 9457 Problem Details 準拠
- 他ユーザーのリソースアクセスは 404 を返す（403 ではなく存在を隠す）
- Pagination なし（個人利用で年間 ~3000 件のため全件取得）

## OpenAPI リンティング

- `backend/openapi.yaml` を変更した場合は `npx @stoplight/spectral-cli lint openapi.yaml`（backend ディレクトリで実行）でリンティングを実施すること
- Spectral のルール設定は `backend/.spectral.yaml` にある

## 命名規約

- DB カラム / JSON キー: snake_case
- Java: camelCase

## エラーハンドリング

- ドメイン例外は `shared/exception/` に定義し、`AppException` を継承する
- Controller で個別キャッチせず `@RestControllerAdvice`（`GlobalExceptionHandler`）で一元処理
- ドメイン層・アプリケーション層では `AppException` のサブクラスのみスローする（生の `RuntimeException` は禁止）
- 他ユーザーのリソースアクセスは `ResourceNotFoundException`（404）で存在を秘匿する
- 詳細は @docs/03-design/common/error-handling.md

## テスト方針

### レイヤー別テスト戦略

| レイヤー | テスト種別 | アノテーション / ツール | DB |
|----------|-----------|----------------------|-----|
| domain | 純粋な単体テスト | なし（Plain JUnit） | 不要 |
| application | 単体テスト | Mockito でリポジトリをモック | 不要 |
| infrastructure | 統合テスト | `@DataJdbcTest` + Testcontainers + Flyway | PostgreSQL |
| presentation | スライステスト | `@WebMvcTest` + MockMvc | 不要 |

- H2 は使わない。統合テストでは必ず Testcontainers（PostgreSQL）を使用する
- Spring Data JDBC は SQL を直接発行するため、DB 方言の差異が直接バグになる

### テストクラスの配置

- テストクラスは `src/test/java/` 配下に、対象クラスと同じパッケージ構成で配置する
- クラス名: `<対象クラス>Test`（例: `TransactionService` → `TransactionServiceTest`）

### テストの書き方

- メソッド名は振る舞いを説明する英語で書く
  - パターン: `<メソッド名>_<条件>_<期待結果>`
  - 例: `createTransaction_withValidInput_returnsCreated`
  - 例: `findByUserIdAndDate_withNoResults_returnsEmptyList`
- Arrange-Act-Assert パターンで構造化し、各セクションを空行で区切る
- モックは外部依存（Repository、外部 API）のみに使い、ドメインオブジェクトは実オブジェクトを使う
- 1 テストメソッドにつき 1 つの振る舞いを検証する
- テストデータはテストメソッド内でローカルに生成し、テスト間で共有しない
- `@DisplayName` は使わない（メソッド名で十分に振る舞いを表現する）

## 参照ドキュメント

常時参照すべきドキュメント:

- `docs/03-design/backend/java-coding-standards.md` — Java コーディング規約

必要時に参照（ルート `AGENTS.md` の参照ドキュメントも併せて確認）:

- `docs/03-design/backend/domain-model.md` — Entity / VO / Aggregate 設計
- `docs/03-design/common/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/backend/validation-strategy.md` — バリデーション方針・層別の責務
- `docs/03-design/backend/database-schema.md` — DDL・インデックス・Flyway
