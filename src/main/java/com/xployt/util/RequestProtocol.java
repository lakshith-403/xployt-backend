package com.xployt.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;

public class RequestProtocol {
  private static final Gson gson = new Gson();

  public static <T> T parseRequest(javax.servlet.http.HttpServletRequest request, Class<T> classType)
      throws java.io.IOException {
    String body = request.getReader()
        .lines()
        .collect(java.util.stream.Collectors.joining());

    if (body == null || body.isEmpty()) {
      return null;
    }

    try {
      return gson.fromJson(body, classType);
    } catch (Exception e) {
      throw new java.io.IOException("Error parsing request body: " + e.getMessage());
    }
  }

  public static java.util.Map<String, Object> parseRequest(javax.servlet.http.HttpServletRequest request)
      throws java.io.IOException {
    String body = request.getReader()
        .lines()
        .collect(java.util.stream.Collectors.joining());

    if (body == null || body.isEmpty()) {
      return new java.util.HashMap<>();
    }

    try {
      java.lang.reflect.Type type = new TypeToken<java.util.Map<String, Object>>() {
      }.getType();
      return gson.fromJson(body, type);
    } catch (Exception e) {
      throw new IOException("Error parsing request body: " + e.getMessage());
    }
  }
}
