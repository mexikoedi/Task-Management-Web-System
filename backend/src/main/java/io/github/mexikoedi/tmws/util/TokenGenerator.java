package io.github.mexikoedi.tmws.util;

import java.util.UUID;

public class TokenGenerator {
  public static String generateToken() {
    return UUID.randomUUID().toString();
  }
}
