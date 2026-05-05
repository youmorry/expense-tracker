---
name: start-issue
description: >
  GitHub Issue の内容を確認し、ブランチを作成して実装を完了する。
  Issueの把握、作業計画、承認、ブランチ作成、TDD実装、ビルド確認までを行う。
disable-model-invocation: true
argument-hint: "[issue-number]"
---

指定された GitHub Issue の作業を開始し、実装を完了してください。
[issue-number] が指定されていない場合はユーザーに確認してください。

## 手順

1. Issue の内容を取得・分析する:
   - `gh issue view [issue-number] --json number,title,body,labels,assignees,milestone` で Issue の詳細を取得する
   - Issue が存在しない、またはクローズ済みの場合はユーザーに確認する
   - タイトル・本文から作業内容を把握する
   - ラベルからバグか機能追加かを判断する
2. Plan Mode で作業計画を立て、ユーザーの承認を得る:
   - Issue の内容をもとに、調査および作業計画を立てる
   - 作業スコープが適切か（この Issue を1つの PR で完結できるか）をユーザーに確認する
   - スコープが大きい場合（複数の独立した機能、複数ドメイン領域にまたがる等）は、具体的な分割案を提示してユーザーに確認する
3. 現在の状態を確認する:
   - `git status` で未コミットの変更がないか確認する
   - 未コミットの変更がある場合はユーザーに確認する
   - 現在のブランチを確認し、main ブランチでない場合はユーザーに続行するか確認する
4. `main` ブランチの最新を取得する:
   - `git checkout main && git pull origin main`
5. ブランチを作成する:
   - `docs/00-constitution.md` のブランチ命名規約に従い、承認された作業計画に基づいた名前でブランチを切る
   - `git checkout -b <ブランチ名>`
6. Issue に作業計画をコメントする:
   - 承認された作業計画と作成したブランチ名を `gh issue comment` で Issue にコメントする
7. TDD サイクルで実装する:
   - `docs/00-constitution.md` の TDD ワークフローに従う
   - コミット時は `.claude/skills/commit/SKILL.md` の規約に従う
   - コミット前にソースコードの自動フォーマット・自動修正を行う（`npm run format` や `./gradlew spotlessApply` など）
8. 実装が完了したらビルドできることを確認する:
   - `./gradlew build`（backend）や `npm run check`（frontend）でビルドが成功することを確認する
   - ビルドに失敗した場合は修正する
9. 完了報告:
   - 実装内容のサマリーをユーザーに報告する
   - PR 作成は `/create-pr` で行うようユーザーに案内する

## 注意事項

- Issue 番号が指定されていない場合はユーザーに確認する
- Issue にアサインされていない場合でも作業は開始できる
