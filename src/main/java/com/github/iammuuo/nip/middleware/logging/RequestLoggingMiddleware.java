package com.github.iammuuo.nip.middleware.logging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class RequestLoggingMiddleware extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestLoggingMiddleware.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();
    try {

      filterChain.doFilter(request, response);
    } finally {

      long duration = System.currentTimeMillis() - start;

      logger.info(
          "Request handled successfully: {} {} {}| Status: {} | Duration: {}ms",
          request.getMethod(),
          request.getRequestURI(),
          request.getRemoteHost(),
          response.getStatus(),
          duration);
    }
  }

}
