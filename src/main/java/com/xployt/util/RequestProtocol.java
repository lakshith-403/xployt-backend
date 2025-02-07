package com.xployt.util;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RequestProtocol {
  private static final Gson gson = new Gson();

  public static <T> T parseRequest(HttpServletRequest request, Class<T> classType)
      throws IOException {
    String body = request.getReader()
        .lines()
        .collect(Collectors.joining());

    if (body == null || body.isEmpty()) {
      return null;
    }

    try {
      return gson.fromJson(body, classType);
    } catch (Exception e) {
      throw new IOException("Error parsing request body: " + e.getMessage());
    }
  }

  public static Map<String, Object> parseRequest(HttpServletRequest request)
      throws IOException {
    String body = request.getReader()
        .lines()

        .collect(Collectors.joining());

    if (body == null || body.isEmpty()) {
      return new HashMap<>();
    }

    try {
      Type type = new TypeToken<Map<String, Object>>() {
      }.getType();
      return gson.fromJson(body, type);

    } catch (Exception e) {
      throw new IOException("Error parsing request body: " + e.getMessage());
    }
  }

  public static Map<String, Object> parseQueryParams(HttpServletRequest request)

      throws IOException {
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isEmpty()) {
      return new HashMap<>();
    }

    Map<String, Object> queryParams = new HashMap<>();
    String[] pairs = queryString.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2) {
        queryParams.put(URLDecoder.decode(keyValue[0], "UTF-8"),
            URLDecoder.decode(keyValue[1], "UTF-8"));
      }
    }
    return queryParams;

  }
}
