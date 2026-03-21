package com.youmorry.expensetracker.presentation.user;

import jakarta.validation.constraints.NotBlank;

/** 通貨コード更新リクエスト。 */
public record UpdateCurrencyRequest(@NotBlank String currencyCode) {}
