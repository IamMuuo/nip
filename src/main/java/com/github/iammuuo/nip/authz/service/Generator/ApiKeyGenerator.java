package com.github.iammuuo.nip.authz.service.Generator;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class ApiKeyGenerator {
  private static final SecureRandom secureRandom = new SecureRandom();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

  public static String generateKey() {
    byte[] buffer = new byte[32];
    secureRandom.nextBytes(buffer);
    return "nip_" + UUID.randomUUID().toString().replace("-", "") + "." + encoder.encodeToString(buffer);
  }
}
