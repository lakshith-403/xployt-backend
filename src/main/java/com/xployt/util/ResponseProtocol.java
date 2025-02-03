package com.xployt.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

public class ResponseProtocol {

  private static final Gson gson = new Gson();

  // Overloaded method without the servlet
  public static void sendSuccess(HttpServletRequest request, HttpServletResponse response, String message, Object data,
      int code) throws IOException {
    createResponse(request, response, "success", message, data, code, null);
  }

  // Overloaded method with the servlet
  public static void sendSuccess(HttpServletRequest request, HttpServletResponse response, HttpServlet servlet,
      String message, Object data, int code) throws IOException {
    createResponse(request, response, "success", message, data, code, servlet);
  }

  // Overloaded method without the servlet
  public static void sendError(HttpServletRequest request, HttpServletResponse response, String message, Object errors,
      int code) throws IOException {
    createResponse(request, response, "error", message, errors, code, null);
  }

  // Overloaded method with the servlet
  public static void sendError(HttpServletRequest request, HttpServletResponse response, HttpServlet servlet,
      String message, Object errors, int code) throws IOException {
    createResponse(request, response, "error", message, errors, code, servlet);
  }

  private static void createResponse(HttpServletRequest request, HttpServletResponse response, String state,
      String message, Object data, int code, HttpServlet servlet) throws IOException {
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("state", state);
    responseMap.put("message", message);
    responseMap.put("code", code);
    if (data != null) {
      responseMap.put("data", data);
    } else {
      responseMap.put("data", new HashMap<>());
    }
    responseMap.put("url", request.getRequestURI());
    // responseMap.put("servlet", request.getServletPath());

    if (servlet != null) {
      responseMap.put("servletClass", servlet.getClass().getName());
    }

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(code);

    response.getWriter().write(gson.toJson(responseMap));
  }
}
