package com.youmorry.expensetracker.domain.model.user;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** Google OAuth2 で認証されたユーザーを表すエンティティ。 */
@Table("users")
public record User(
    @Id UserId id,
    String googleId,
    String email,
    String displayName,
    String currencyCode,
    Instant createdAt) {}
