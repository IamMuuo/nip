package com.github.iammuuo.nip.authz.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.iammuuo.nip.authz.entity.ApiKey;
import com.github.iammuuo.nip.authz.repository.ApiKeyRepository;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String API_KEY_SCHEME = "ApiKey ";

  private final ApiKeyRepository apiKeyRepository;
  private final PasswordEncoder passwordEncoder;
  private final ConcurrentHashMap<String, AttemptWindow> failedAttempts = new ConcurrentHashMap<>();

  public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder) {
    this.apiKeyRepository = apiKeyRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);

    if (authorization == null || !authorization.startsWith(API_KEY_SCHEME)) {
      filterChain.doFilter(request, response);
      return;
    }

    String presentedKey = authorization.substring(API_KEY_SCHEME.length());
    int separator = presentedKey.indexOf('.');
    if (separator <= 0 || separator == presentedKey.length() - 1
        || separator != presentedKey.lastIndexOf('.')) {
      unauthorized(response);
      return;
    }

    String keyId = presentedKey.substring(0, separator);
    String secret = presentedKey.substring(separator + 1);
    if (keyId.length() > 36 || secret.length() > 128 || isRateLimited(request, keyId)) {
      unauthorized(response);
      return;
    }
    ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

    if (!isValid(apiKey, secret)) {
      recordFailure(request, keyId);
      unauthorized(response);
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            apiKey.getAccount().getId().toString(), null,
            Arrays.stream(apiKey.getScopes() == null ? new String[0] : apiKey.getScopes().split(","))
                .filter(scope -> !scope.isBlank())
                .map(scope -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "SCOPE_" + scope)).toList());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  private boolean isRateLimited(HttpServletRequest request, String keyId) {
    String source = request.getRemoteAddr() + ":" + keyId;
    AttemptWindow window = failedAttempts.get(source);
    return window != null && !window.allow();
  }

  private void recordFailure(HttpServletRequest request, String keyId) {
    failedAttempts.compute(request.getRemoteAddr() + ":" + keyId,
        (ignored, current) -> current == null ? new AttemptWindow() : current.record());
  }

  private static final class AttemptWindow {
    private final long startedAt = System.currentTimeMillis();
    private final AtomicInteger attempts = new AtomicInteger(1);
    private AttemptWindow record() { attempts.incrementAndGet(); return this; }
    private boolean allow() {
      if (System.currentTimeMillis() - startedAt > 60_000) { attempts.set(0); return true; }
      return attempts.get() < 5;
    }
  }

  private boolean isValid(ApiKey apiKey, String secret) {
    return apiKey != null
        && apiKey.isActive()
        && apiKey.getAccount() != null
        && apiKey.getAccount().isActive()
        && (apiKey.getExpiresAt() == null || apiKey.getExpiresAt().isAfter(Instant.now()))
        && passwordEncoder.matches(secret, apiKey.getKeyHash());
  }

  private void unauthorized(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
  }
}
