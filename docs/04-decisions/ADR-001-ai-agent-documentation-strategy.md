# ADR-001: AI エージェント対応とドキュメント構造の整理方針

## ステータス

Accepted (2026-05-04)

## コンテキスト

本プロジェクトはこれまで Claude Code を主な AI エージェントとして利用し、`CLAUDE.md` および `.claude/rules/*.md` でコンテキストを提供してきた。Issue #312 で以下の課題が提起された。

- Codex など Claude Code 以外の AI エージェントも利用するケースがある
- ドキュメントが Claude Code 固有形式に偏っているため、他エージェントは読めない
- アーキテクチャ決定の正本が散在しており、ADR の運用が未整備
- プロジェクト全体の不変原則（TDD、GitHub Flow、命名規約、AI 由来の明記など）が複数ファイルに分散している

なお機能単位の仕様管理（Spec Kit 形式）は影響範囲が大きいため、Issue #331 で別途検討する。

## 決定

### 1. AGENTS.md 規約への移行

複数 AI エージェント共通の規約フォーマットとして [AGENTS.md](https://agents.md/) を採用する。Claude Code との互換は対応する `CLAUDE.md` をシンボリックリンクとして残すことで担保する。

| 移行元 | 移行先 |
| --- | --- |
| ルート `CLAUDE.md` | ルート `AGENTS.md`（`CLAUDE.md` は `AGENTS.md` へのシンボリックリンク） |
| `.claude/rules/backend.md` | `backend/AGENTS.md`（`backend/CLAUDE.md` はシンボリックリンク） |
| `.claude/rules/frontend.md` | `frontend/AGENTS.md`（`frontend/CLAUDE.md` はシンボリックリンク） |
| `.claude/rules/design-principles.md` | `docs/00-constitution.md` に統合 |
| `.claude/rules/context7.md` | ルート `AGENTS.md` に統合 |

`.claude/rules/` ディレクトリは廃止する。`.claude/{hooks, skills, settings.json, settings.local.json}` は Claude Code 固有の機構として継続使用する。

### 2. Constitution の新設

`docs/00-constitution.md` を新設し、プロジェクト全体の不変原則を集約する。

- TDD ワークフロー
- GitHub Flow（ブランチ命名、`main` 維持）
- 言語ポリシー（識別子は英語、コメント・Javadoc は日本語可）
- AI 由来の明記（コミットの `Co-Authored-By`、PR・コメントのフッター）
- コミット規約（Conventional Commits、type/scope ルール）
- 既存 `design-principles.md` の内容（ドメインモデル・モジュール設計）

各 `AGENTS.md` からは Constitution への参照とし、規約の SSOT を一本化する。

### 3. ADR の運用開始

- 配置: `docs/04-decisions/ADR-NNN-<title>.md`
- 形式: MADR ライト版（Status / Context / Decision / Consequences）
- 本 ADR が第一号であり、ADR 運用そのものを定めるメタ ADR を兼ねる

### 4. 機能単位仕様の構造

`specs/<NNN-feature>/` による Spec Kit 形式の機能単位仕様は本 ADR では決定しない。Issue #331 で試験導入の上、別 ADR として記録する。

## 影響

### 移行作業（本 PR で実施済み）

1. `CLAUDE.md` → `AGENTS.md` リネームとシンボリックリンク作成
2. `.claude/rules/{backend,frontend}.md` を各ディレクトリ `AGENTS.md` に移植
3. `.claude/rules/{design-principles,context7}.md` の内容を `docs/00-constitution.md` および ルート `AGENTS.md` に統合
4. `.claude/rules/` の削除
5. `docs/00-constitution.md` の新設
6. `README.md` と `start-issue` スキルの `.claude/rules/` 参照を AGENTS.md / Constitution に更新

### ポジティブ

- Codex / Cursor / Aider など Claude Code 以外の AI エージェントも同等のコンテキストを参照できる
- 規約の正本が AGENTS.md と Constitution に一本化され、ドリフトを抑制できる
- アーキテクチャ決定が ADR として残り、Issue/PR から追跡可能になる

### ネガティブ

- `.claude/rules/` の path-pattern ベース auto-load を失う
  - 代替として AGENTS.md のディレクトリカスケード（最も近い AGENTS.md が優先）で、実用上ほぼ等価な挙動になる見込み
  - ピンポイント発火が必要なルールが今後出てきた場合は `.claude/rules/` を限定的に併用する余地は残す
- `git mv` とシンボリックリンクで一時的にファイル履歴が追いにくくなる箇所がある（`git log --follow` で対応可）

### 中立

- `README.md` は `.claude/rules/` 参照を AGENTS.md / Constitution に更新済み
- `docs/01-planning / 02-requirements / 03-design` の構造は維持

## 関連

- Issue #312 — 本 ADR の検討元
- Issue #331 — Spec Kit 試験導入（別途検討）
- 過去 Issue: #16, #151, #160
