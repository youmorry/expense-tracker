package com.youmorry.expensetracker.presentation.user;

import jakarta.validation.constraints.NotNull;
import java.util.Currency;

/** 通貨コード更新リクエスト。 */
public record UpdateCurrencyRequest(@NotNull Currency currencyCode) {}
