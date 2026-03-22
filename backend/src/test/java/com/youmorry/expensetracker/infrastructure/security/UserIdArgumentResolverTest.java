package com.youmorry.expensetracker.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.model.user.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.NativeWebRequest;

class UserIdArgumentResolverTest {

  private final UserIdArgumentResolver resolver = new UserIdArgumentResolver();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void supportsParameter_withUserIdType_returnsTrue() {
    var parameter = mock(MethodParameter.class);
    when(parameter.getParameterType()).thenReturn((Class) UserId.class);

    assertTrue(resolver.supportsParameter(parameter));
  }

  @Test
  void supportsParameter_withOtherType_returnsFalse() {
    var parameter = mock(MethodParameter.class);
    when(parameter.getParameterType()).thenReturn((Class) String.class);

    assertFalse(resolver.supportsParameter(parameter));
  }

  @Test
  void resolveArgument_withJwtAuthentication_returnsUserId() throws Exception {
    var jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42").build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

    var result =
        resolver.resolveArgument(mock(MethodParameter.class), null, mock(NativeWebRequest.class), null);

    assertEquals(new UserId(42L), result);
  }

  @Test
  void resolveArgument_withNoAuthentication_returnsNull() throws Exception {
    SecurityContextHolder.clearContext();

    var result =
        resolver.resolveArgument(mock(MethodParameter.class), null, mock(NativeWebRequest.class), null);

    assertNull(result);
  }
}
