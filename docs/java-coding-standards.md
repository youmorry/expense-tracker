# Java Coding Standards

[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) に準拠する。

フォーマッターは [google-java-format](https://github.com/google/google-java-format)、静的解析は [Checkstyle](https://checkstyle.org/)（`google_checks.xml` ベース）を使用する。

## プロジェクト固有の補足

- インデント: 半角スペース 2 文字（Google Style 標準と同一）
- Javadoc: `public` API には記述する。ただし Record の自動生成アクセサや自明な getter/setter は省略可
