package com.youmorry.expensetracker.presentation.auth;

import jakarta.validation.constraints.NotBlank;

/** Google 認証リクエスト。 */
public record AuthRequest(@NotBlank String idToken) {}
