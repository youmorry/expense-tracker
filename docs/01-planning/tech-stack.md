# 技術スタック

## 概要

### バックエンド

| 技術 | バージョン |
|------|------------|
| Java | 25 (LTS) |
| Spring Boot | 4.0.x |
| PostgreSQL | 18.x |

詳細: [tech-stack-backend.md](tech-stack-backend.md)

### フロントエンド

| 技術 | バージョン |
|------|------------|
| React | 19.x |
| TypeScript | 6.x |
| Vite | 8.x |

詳細: [tech-stack-frontend.md](tech-stack-frontend.md)

### 共通

| 技術 | バージョン |
|------|------------|
| Docker / Docker Compose | - |

---

## 開発環境

### ローカル開発

| 技術 | 用途 |
|------|------|
| Docker Compose | PostgreSQL などのミドルウェアをローカルで起動 |
| Git / GitHub | バージョン管理 |
| GitHub Issues / Projects | タスク管理 |

ローカル開発では、アプリ本体（Spring Boot / React）はホストマシン上で直接起動し、
PostgreSQL のみ Docker Compose で管理する。これにより開発時のビルド・起動サイクルを高速に保つ。

---

## インフラ・デプロイ

### 構成

```
FE: Render（Static Site）   ← 無料・永続・CDN配信
BE: Render（Web Service）   ← 無料・15分無通信でスリープあり
DB: Neon                    ← PostgreSQL マネージドサービス・無料・永続
```

| サービス | 役割 | 無料枠の制限 |
|----------|------|-------------|
| Render Static Site | FE（React）の配信 | 制限なし |
| Render Web Service | BE（Spring Boot）の実行 | 15分無通信でスリープ（復帰に30〜60秒） |
| Neon | DB（PostgreSQL）のホスティング | 制限なし（スリープは数百ms程度で実用上問題なし） |

> **スリープについて**
> Render 無料枠の BE は15分無通信でスリープ状態になる。無料運用の制約として許容する。
> 将来的に常時起動が必要になった場合は Render の有料プラン（$7/月〜）に移行する。

### デプロイフロー

```
main にマージ
  ├─→ Render: FE を自動ビルド（npm run build）→ CDN にデプロイ
  └─→ Render: BE を自動ビルド（Dockerfile）→ コンテナ起動
           └─→ Flyway マイグレーション実行 → アプリ起動

DB（Neon）はデプロイとは独立して管理（スキーマ変更は Flyway 経由）
```

### 環境変数

コードに書かず、Render のダッシュボードで設定する。

| 変数名 | 設定先 | 内容 |
|--------|--------|------|
| `DB_URL` | BE | Neon の接続URL |
| `DB_USER` | BE | DB ユーザー名 |
| `DB_PASSWORD` | BE | DB パスワード |
| `GOOGLE_CLIENT_ID` | BE | OAuth2 クライアントID |
| `GOOGLE_CLIENT_SECRET` | BE | OAuth2 クライアントシークレット |
| `VITE_API_BASE_URL` | FE | BE の URL |

### Docker の利用範囲

| 環境 | 構成 |
|------|------|
| ローカル | Docker Compose で PostgreSQL のみ起動。アプリはホストマシンで直接実行 |
| 本番（Render） | BE は Dockerfile でコンテナ化してデプロイ。DB は Neon のマネージドサービスを利用 |

---

## CI/CD

| 技術 | 用途 |
|------|------|
| GitHub Actions | テスト自動化 |
| Render | GitHub 連携による自動デプロイ（CD） |

### パイプライン構成

```
PR 作成 → GitHub Actions: ビルド + テスト（CI）
main マージ → Render: 自動デプロイ（CD）
```

| ステージ | 実行環境 | 内容 |
|----------|----------|------|
| ビルド | GitHub Actions | Spring Boot jar ビルド / React ビルド |
| テスト | GitHub Actions | 単体テスト・結合テストの自動実行 |
| デプロイ | Render | main マージをトリガーに FE・BE を自動デプロイ |

---

## インフラの技術選定の理由

### Render + Neon

無料で永続運用できる構成として採用。FE は Render Static Site でCDN配信、BE は Render Web Service で
Dockerコンテナ実行、DB は Neon のマネージドPostgreSQLを利用する。BE のスリープは無料運用の制約として許容する。
