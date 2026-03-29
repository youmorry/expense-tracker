package com.youmorry.expensetracker.presentation.user;

import com.youmorry.expensetracker.domain.user.CurrencyCode;
import jakarta.validation.constraints.NotNull;

/** 通貨コード更新リクエスト。 */
public record UpdateCurrencyRequest(@NotNull CurrencyCode currencyCode) {}
