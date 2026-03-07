package com.youmorry.expensetracker.presentation;

import com.youmorry.expensetracker.shared.exception.ForbiddenException;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import com.youmorry.expensetracker.shared.exception.UnauthorizedException;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("The requested resource was not found.");
        }

        @GetMapping("/test/validation")
        public void validation() {
            throw new ValidationException(
                    "One or more fields have validation errors.",
                    List.of(
                            new ValidationException.FieldError("must be greater than 0", "#/amount"),
                            new ValidationException.FieldError("must not be null", "#/date")
                    )
            );
        }

        @GetMapping("/test/unauthorized")
        public void unauthorized() {
            throw new UnauthorizedException("The access token is missing or invalid.");
        }

        @GetMapping("/test/forbidden")
        public void forbidden() {
            throw new ForbiddenException("You do not have permission to access this resource.");
        }

        @GetMapping("/test/unexpected")
        public void unexpected() {
            throw new RuntimeException("Something went wrong");
        }

        @PostMapping("/test/invalid-json")
        public void invalidJson(@RequestBody String body) {
        }

        @GetMapping("/test/type-mismatch")
        public void typeMismatch(@RequestParam Integer id) {
        }

        @PostMapping("/test/bean-validation")
        public void beanValidation(@Valid @RequestBody BeanValidationRequest request) {
        }

        record BeanValidationRequest(@NotNull String name) {}
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleResourceNotFoundException_returns404WithProblemJson() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("The requested resource was not found."))
                .andExpect(jsonPath("$.instance").value("/test/not-found"));
    }

    @Test
    void handleValidationException_returns422WithErrors() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errors/validation-error"))
                .andExpect(jsonPath("$.title").value("Your request is not valid."))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].detail").value("must be greater than 0"))
                .andExpect(jsonPath("$.errors[0].pointer").value("#/amount"));
    }

    @Test
    void handleUnauthorizedException_returns401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errors/unauthorized"))
                .andExpect(jsonPath("$.title").value("Authentication required."))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("The access token is missing or invalid."));
    }

    @Test
    void handleForbiddenException_returns403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errors/forbidden"))
                .andExpect(jsonPath("$.title").value("Forbidden."))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void handleUnexpectedException_returns500() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }

    @Test
    void handleHttpMessageNotReadable_returns400() throws Exception {
        mockMvc.perform(post("/test/invalid-json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Failed to parse request body."));
    }

    @Test
    void handleMethodArgumentTypeMismatch_returns400() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'id'."));
    }

    @Test
    void handleMethodArgumentNotValid_returns422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/test/bean-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/errors/validation-error"))
                .andExpect(jsonPath("$.title").value("Your request is not valid."))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].pointer").value("#/name"));
    }
}
