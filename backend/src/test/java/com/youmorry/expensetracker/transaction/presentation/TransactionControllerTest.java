package com.youmorry.expensetracker.transaction.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.auth.infrastructure.SecurityConfig;
import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import com.youmorry.expensetracker.shared.infrastructure.web.WebMvcConfig;
import com.youmorry.expensetracker.transaction.application.TransactionCreateCommand;
import com.youmorry.expensetracker.transaction.application.TransactionResult;
import com.youmorry.expensetracker.transaction.application.TransactionSearchQuery;
import com.youmorry.expensetracker.transaction.application.TransactionService;
import com.youmorry.expensetracker.transaction.application.TransactionUpdateCommand;
import com.youmorry.expensetracker.transaction.domain.Money;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import com.youmorry.expensetracker.transaction.domain.Transaction;
import com.youmorry.expensetracker.transaction.domain.TransactionId;
import com.youmorry.expensetracker.user.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, WebMvcConfig.class})
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private TransactionService transactionService;

  @Test
  void create_withValidRequest_returns201WithTransaction() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(42L),
            new UserId(1L),
            LocalDate.of(2026, 2, 23),
            new Money(new BigDecimal("1200.50")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            "With colleagues",
            Instant.parse("2026-02-23T10:30:00Z"),
            Instant.parse("2026-02-23T10:30:00Z"));
    var result = new TransactionResult(transaction, "Food");
    when(transactionService.create(eq(new UserId(1L)), any(TransactionCreateCommand.class)))
        .thenReturn(result);

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-02-23",
                      "amount": "1200.50",
                      "category_id": 1,
                      "need_want_type": "NEED",
                      "title": "Lunch",
                      "memo": "With colleagues"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.date").value("2026-02-23"))
        .andExpect(jsonPath("$.amount").value("1200.50"))
        .andExpect(jsonPath("$.category_id").value(1))
        .andExpect(jsonPath("$.category_name").value("Food"))
        .andExpect(jsonPath("$.need_want_type").value("NEED"))
        .andExpect(jsonPath("$.title").value("Lunch"))
        .andExpect(jsonPath("$.memo").value("With colleagues"))
        .andExpect(jsonPath("$.created_at").value("2026-02-23T10:30:00Z"))
        .andExpect(jsonPath("$.updated_at").value("2026-02-23T10:30:00Z"));
  }

  @Test
  void create_withMissingDate_returns422() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": "1200"
                    }
                    """))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.errors[?(@.pointer == '#/date')]").exists());
  }

  @Test
  void create_withMissingAmount_returns422() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-02-23"
                    }
                    """))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.errors[?(@.pointer == '#/amount')]").exists());
  }

  @Test
  void create_withoutJwt_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-02-23",
                      "amount": "1200"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void create_withOptionalFieldsOmitted_returns201() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(43L),
            new UserId(1L),
            LocalDate.of(2026, 2, 23),
            new Money(new BigDecimal("500")),
            new CategoryId(11L),
            NeedWantType.UNSET,
            null,
            null,
            Instant.parse("2026-02-23T11:00:00Z"),
            Instant.parse("2026-02-23T11:00:00Z"));
    var result = new TransactionResult(transaction, "Uncategorized");
    when(transactionService.create(eq(new UserId(1L)), any(TransactionCreateCommand.class)))
        .thenReturn(result);

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-02-23",
                      "amount": "500"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(43))
        .andExpect(jsonPath("$.date").value("2026-02-23"))
        .andExpect(jsonPath("$.amount").value("500"))
        .andExpect(jsonPath("$.category_id").value(11))
        .andExpect(jsonPath("$.category_name").value("Uncategorized"))
        .andExpect(jsonPath("$.need_want_type").value("UNSET"))
        .andExpect(jsonPath("$.title").doesNotExist())
        .andExpect(jsonPath("$.memo").doesNotExist());
  }

  @Test
  void list_withNoParams_returns200WithItems() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(42L),
            new UserId(1L),
            LocalDate.of(2026, 2, 23),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-02-23T10:30:00Z"),
            Instant.parse("2026-02-23T10:30:00Z"));
    when(transactionService.search(eq(new UserId(1L)), any(TransactionSearchQuery.class)))
        .thenReturn(List.of(new TransactionResult(transaction, "Food")));

    mockMvc
        .perform(get("/api/v1/transactions").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(42))
        .andExpect(jsonPath("$.items[0].date").value("2026-02-23"))
        .andExpect(jsonPath("$.items[0].amount").value("1200"))
        .andExpect(jsonPath("$.items[0].category_id").value(1))
        .andExpect(jsonPath("$.items[0].category_name").value("Food"))
        .andExpect(jsonPath("$.items[0].need_want_type").value("NEED"));
  }

  @Test
  void list_withAllParams_returns200WithFilteredItems() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(10L),
            new UserId(1L),
            LocalDate.of(2026, 2, 15),
            new Money(new BigDecimal("500")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-02-15T10:00:00Z"),
            Instant.parse("2026-02-15T10:00:00Z"));
    when(transactionService.search(eq(new UserId(1L)), any(TransactionSearchQuery.class)))
        .thenReturn(List.of(new TransactionResult(transaction, "Food")));

    mockMvc
        .perform(
            get("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .param("from", "2026-02-01")
                .param("to", "2026-02-28")
                .param("category_id", "1", "3")
                .param("need_want_type", "NEED")
                .param("keyword", "Lunch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(10));
  }

  @Test
  void list_withInvalidCategoryId_returns422() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/transactions")
                .with(jwt().jwt(j -> j.subject("1")))
                .param("category_id", "-1"))
        .andExpect(status().is(422));
  }

  @Test
  void list_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/transactions")).andExpect(status().isUnauthorized());
  }

  @Test
  void list_withEmptyResult_returns200WithEmptyItems() throws Exception {
    when(transactionService.search(eq(new UserId(1L)), any(TransactionSearchQuery.class)))
        .thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/transactions").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(0));
  }

  @Test
  void getById_withExistingTransaction_returns200WithTransaction() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(42L),
            new UserId(1L),
            LocalDate.of(2026, 2, 23),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            "Company cafeteria",
            Instant.parse("2026-02-23T10:30:00Z"),
            Instant.parse("2026-02-23T10:30:00Z"));
    var result = new TransactionResult(transaction, "Food");
    when(transactionService.findById(new UserId(1L), new TransactionId(42L))).thenReturn(result);

    mockMvc
        .perform(get("/api/v1/transactions/42").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.date").value("2026-02-23"))
        .andExpect(jsonPath("$.amount").value("1200"))
        .andExpect(jsonPath("$.category_id").value(1))
        .andExpect(jsonPath("$.category_name").value("Food"))
        .andExpect(jsonPath("$.need_want_type").value("NEED"))
        .andExpect(jsonPath("$.title").value("Lunch"))
        .andExpect(jsonPath("$.memo").value("Company cafeteria"))
        .andExpect(jsonPath("$.created_at").value("2026-02-23T10:30:00Z"))
        .andExpect(jsonPath("$.updated_at").value("2026-02-23T10:30:00Z"));
  }

  @Test
  void getById_withNonExistentTransaction_returns404() throws Exception {
    when(transactionService.findById(new UserId(1L), new TransactionId(999L)))
        .thenThrow(new ResourceNotFoundException("Transaction not found: 999"));

    mockMvc
        .perform(get("/api/v1/transactions/999").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void getById_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/transactions/42")).andExpect(status().isUnauthorized());
  }

  @Test
  void update_withValidRequest_returns200WithUpdatedTransaction() throws Exception {
    var transaction =
        new Transaction(
            new TransactionId(42L),
            new UserId(1L),
            LocalDate.of(2026, 3, 26),
            new Money(new BigDecimal("1500")),
            new CategoryId(2L),
            NeedWantType.WANT,
            "Dinner",
            "at restaurant",
            Instant.parse("2026-02-23T10:30:00Z"),
            Instant.parse("2026-03-26T10:00:00Z"));
    var result = new TransactionResult(transaction, "Transport");
    when(transactionService.update(
            eq(new UserId(1L)),
            eq(new TransactionId(42L)),
            eq(
                new TransactionUpdateCommand(
                    LocalDate.of(2026, 3, 26),
                    new BigDecimal("1500"),
                    new CategoryId(2L),
                    NeedWantType.WANT,
                    "Dinner",
                    "at restaurant"))))
        .thenReturn(result);

    mockMvc
        .perform(
            put("/api/v1/transactions/42")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-03-26",
                      "amount": "1500",
                      "category_id": 2,
                      "need_want_type": "WANT",
                      "title": "Dinner",
                      "memo": "at restaurant"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.date").value("2026-03-26"))
        .andExpect(jsonPath("$.amount").value("1500"))
        .andExpect(jsonPath("$.category_id").value(2))
        .andExpect(jsonPath("$.category_name").value("Transport"))
        .andExpect(jsonPath("$.need_want_type").value("WANT"))
        .andExpect(jsonPath("$.title").value("Dinner"))
        .andExpect(jsonPath("$.memo").value("at restaurant"));
  }

  @Test
  void update_withMissingDate_returns422() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/transactions/42")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": "1500"
                    }
                    """))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.errors[?(@.pointer == '#/date')]").exists());
  }

  @Test
  void update_withNonExistentTransaction_returns404() throws Exception {
    when(transactionService.update(
            eq(new UserId(1L)), eq(new TransactionId(999L)), any(TransactionUpdateCommand.class)))
        .thenThrow(new ResourceNotFoundException("Transaction not found: 999"));

    mockMvc
        .perform(
            put("/api/v1/transactions/999")
                .with(jwt().jwt(j -> j.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-03-26",
                      "amount": "1500"
                    }
                    """))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_withoutJwt_returns401() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/transactions/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "date": "2026-03-26",
                      "amount": "1500"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void delete_withExistingTransaction_returns204() throws Exception {
    doNothing().when(transactionService).delete(new UserId(1L), new TransactionId(42L));

    mockMvc
        .perform(delete("/api/v1/transactions/42").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_withNonExistentTransaction_returns404() throws Exception {
    doThrow(new ResourceNotFoundException("Transaction not found: 999"))
        .when(transactionService)
        .delete(new UserId(1L), new TransactionId(999L));

    mockMvc
        .perform(delete("/api/v1/transactions/999").with(jwt().jwt(j -> j.subject("1"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_withoutJwt_returns401() throws Exception {
    mockMvc.perform(delete("/api/v1/transactions/42")).andExpect(status().isUnauthorized());
  }
}
