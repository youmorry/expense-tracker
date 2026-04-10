package com.youmorry.expensetracker.auth.presentation;

import jakarta.validation.constraints.NotBlank;

/** Google 認証リクエスト。 */
public record AuthRequest(@NotBlank String idToken) {}
