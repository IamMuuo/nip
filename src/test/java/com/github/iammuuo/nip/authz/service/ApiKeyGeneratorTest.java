package com.github.iammuuo.nip.authz.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.iammuuo.nip.authz.service.Generator.ApiKeyGenerator;

class ApiKeyGeneratorTest {

  @Test
  void generatesLookupableKeyWithPublicIdAndSecret() {
    String first = ApiKeyGenerator.generateKey();
    String second = ApiKeyGenerator.generateKey();

    assertEquals(2, first.split("\\.", -1).length);
    assertTrue(first.startsWith("nip_"));
    assertNotEquals(first, second);
  }
}
