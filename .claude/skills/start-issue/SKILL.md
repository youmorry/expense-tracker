---
name: start-issue
description: >
  GitHub Issue の内容を確認し、ブランチを作成して作業を開始する。
  Issueの把握、作業計画、承認、実装、ビルド、PR作成までの一連の流れを行う。
disable-model-invocation: true
argument-hint: "<Issue番号>"
---

指定された GitHub Issue の作業を開始してください。
Issue の指定: `$ARGUMENTS`

## 手順

1. Issue の内容を取得・分析する:
   - `gh issue view <Issue番号> --json number,title,body,labels,assignees,milestone` で Issue の詳細を取得する
   - Issue が存在しない、またはクローズ済みの場合はユーザーに確認する
   - タイトル・本文から作業内容を把握する
   - ラベルからバグか機能追加かを判断する
2. Plan Modeで作業計画を立て、ユーザーの承認を得る:
   - Issue の内容をもとに、調査及び作業計画をたてる。
   - 作業スコープが適切か（この Issue を1つの PR で完結できるか）をユーザーに確認する
   - スコープが大きい場合（複数の独立した機能、複数ドメイン領域にまたがる等）は、具体的な分割案を提示してユーザーに確認する
3. ユーザー承認を得たら、作業を開始する:
    - 作業着手前にIssueに作業計画をコメントする。
    - CLAUDE.md の TDD ワークフローに従って作業を開始する。
3. 現在の状態を確認する:
   - `git status` で未コミットの変更がないか確認する
   - 未コミットの変更がある場合はユーザーに確認する
   - 現在のブランチを確認する
4. `main` ブランチの最新を取得する:
   - `git checkout main && git pull origin main`
5. ブランチを作成する:
   - CLAUDE.md の Conventions に定義されたブランチ命名規約に従い、承認された作業計画に基づいた名前でブランチを切る
   - `git checkout -b <ブランチ名>`
6. TDD サイクルで実装を開始する:
   - CLAUDE.md の TDD ワークフローに従う
   - コミット時は `.claude/skills/commit/SKILL.md` の規約に従う
7. 実装が完了したらビルドできることを確認する:
   - `./gradlew build`（backend）や `npm run build`（frontend）でビルドが成功することを確認する
   - ビルドに失敗した場合は修正する
8. PRの作成を行う
    - ./claude/skills/create-pr/SKILL.md の規約に従って PR を作成する

## 注意事項

- Issue 番号が指定されていない場合はユーザーに確認する
- `main` ブランチ以外から作業を開始しようとしている場合は警告する
- Issue にアサインされていない場合でも作業は開始できる
