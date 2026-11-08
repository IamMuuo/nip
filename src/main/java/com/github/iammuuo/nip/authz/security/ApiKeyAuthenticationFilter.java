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
import com.github.iammuuo.nip.authz.service.ApiKeyService;

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
    ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

    if (!isValid(apiKey, secret)) {
      unauthorized(response);
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            apiKey.getAccount().getId().toString(), null, Collections.emptyList());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
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
