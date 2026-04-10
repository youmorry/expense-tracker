package com.youmorry.expensetracker.category.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.youmorry.expensetracker.category.application.CategoryService;
import com.youmorry.expensetracker.category.domain.CategoryType;
import com.youmorry.expensetracker.auth.infrastructure.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private CategoryService categoryService;

  @Test
  void getCategories_withoutJwt_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/categories")).andExpect(status().isUnauthorized());
  }

  @Test
  void getCategories_withValidJwt_returnsOkWithItems() throws Exception {
    when(categoryService.findAll())
        .thenReturn(List.of(CategoryType.FOOD, CategoryType.TRANSPORT, CategoryType.UNCATEGORIZED));

    mockMvc
        .perform(get("/api/v1/categories").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(3))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.items[0].name").value("Food"))
        .andExpect(jsonPath("$.items[0].display_order").value(1))
        .andExpect(jsonPath("$.items[2].id").value(11))
        .andExpect(jsonPath("$.items[2].name").value("Uncategorized"))
        .andExpect(jsonPath("$.items[2].display_order").value(11));
  }
}
