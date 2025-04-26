package com.xployt.util;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xployt.model.User;

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
    System.out.println("---- Executing parseQueryParams ----");
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isEmpty()) {
      System.out.println("Query string is empty");
      return new HashMap<>();

    }
    System.out.println("Query string: " + queryString);
    Map<String, Object> queryParams = new HashMap<>();
    String[] pairs = queryString.split("&");

    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2) {
        queryParams.put(URLDecoder.decode(keyValue[0], "UTF-8"),
            URLDecoder.decode(keyValue[1], "UTF-8"));
      }
    }
    System.out.println("Query params: " + queryParams);
    return queryParams;

  }

  public static ArrayList<String> parsePathParams(HttpServletRequest request)
      throws IOException {

    System.out.println("---- Executing parsePathParams ----");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      return new ArrayList<>();
    }
    System.out.println("Path info: " + pathInfo);
    ArrayList<String> pathParams = new ArrayList<>();
    String[] parts = pathInfo.split("/");

    for (String part : parts) {
      if (!part.isEmpty()) {
        pathParams.add(part);
      }
    }

    System.out.println("Path params: " + pathParams);
    return pathParams;

  }

  public static boolean authorizeRequest(HttpServletRequest request, HttpServletResponse response, String[] roles, boolean isAllowList) throws IOException {
    User currentUser = AuthUtil.getSignedInUser(request);
    
    if (currentUser == null) {
      ResponseProtocol.sendError(request, response, "Authentication required", null, HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    String userRole = currentUser.getRole();
    
    if (isAllowList) {
      // Check if user's role is in the allowed roles list
      for (String role : roles) {
        if (role.equals(userRole)) {
          System.out.println("User with role: " + userRole + " is allowed access to this resource");
          return true;
        }
      }
      System.out.println("User with role: " + userRole + " is denied access to this resource");
      ResponseProtocol.sendError(request, response, "Unauthorized access", null, HttpServletResponse.SC_FORBIDDEN);
      return false;
    } else {
      // Check if user's role is in the denied roles list
      for (String role : roles) {
        if (role.equals(userRole)) {
          System.out.println("User with role: " + userRole + " is denied access to this resource");
          ResponseProtocol.sendError(request, response, "Unauthorized access", null, HttpServletResponse.SC_FORBIDDEN);
          return false;
        }
      }
      System.out.println("User with role: " + userRole + " is allowed access to this resource");
      return true;
    }
  }

  public static boolean authorizeRequest(HttpServletRequest request, HttpServletResponse response, String[] roles) throws IOException {
    return authorizeRequest(request, response, roles, true);
  }

  public static boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User currentUser = AuthUtil.getSignedInUser(request);
    
    if (currentUser == null) {
      System.out.println("No authenticated user found in session");
      ResponseProtocol.sendError(request, response, "Authentication required", null, HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    System.out.println("User " + currentUser.getEmail() + " is authenticated");
    return true;
  }
}
