package com.github.iammuuo.nip.authz.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.github.iammuuo.nip.account.entity.Account;
import com.github.iammuuo.nip.authz.entity.ApiKey;
import com.github.iammuuo.nip.authz.repository.ApiKeyRepository;

class ApiKeyAuthenticationFilterTest {

  @Test
  void authenticatesValidApiKey() throws Exception {
    String secret = "secret";
    Account account = Account.builder().id(UUID.randomUUID()).active(true).build();
    ApiKeyRepository repository = (ApiKeyRepository) java.lang.reflect.Proxy.newProxyInstance(
        ApiKeyRepository.class.getClassLoader(), new Class<?>[] { ApiKeyRepository.class },
        (proxy, method, args) -> method.getName().equals("findByKeyId")
            ? Optional.of(ApiKey.builder().id(UUID.randomUUID()).keyId("nip_public")
                .keyHash(new BCryptPasswordEncoder().encode(secret)).account(account).active(true)
                .expiresAt(Instant.now().plusSeconds(60)).build())
            : defaultValue(method.getReturnType()));

    ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(repository,
        new BCryptPasswordEncoder());
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "ApiKey nip_public.secret");
    MockHttpServletResponse response = new MockHttpServletResponse();

    try {
      filter.doFilter(request, response, new MockFilterChain());

      assertEquals(200, response.getStatus());
      assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
      assertEquals(account.getId().toString(),
          SecurityContextHolder.getContext().getAuthentication().getName());
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private static Object defaultValue(Class<?> returnType) {
    if (!returnType.isPrimitive()) return null;
    if (returnType == boolean.class) return false;
    if (returnType == char.class) return '\0';
    return 0;
  }
}
