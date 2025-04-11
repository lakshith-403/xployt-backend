package com.xployt.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

/*
 * This is a template servlet for creating new servlets.
 * It is used to set up a new Controller.
 * Try out these endpoints to see how it works.
 */
@WebServlet("/api/test-new/*")
public class NewTemplateServlet extends HttpServlet {

  private static final List<Object[]> sqlParams = new ArrayList<>();
  private static final List<Map<String, Object>> results = new ArrayList<>();
  private static final Map<String, Object> requestBody = new HashMap<>();
  private static final List<String> pathParams = new ArrayList<>();
  private static final Map<String, Object> queryParams = new HashMap<>();
  private static String[] sqlStatements = {};

  /**
   * Handles GET requests to retrieve a resource.
   * 
   * @param request  the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws IOException if an input or output error is detected
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ TemplateServlet | doGet ------------");
    pathParams.clear();
    pathParams.addAll(RequestProtocol.parsePathParams(request));
    System.out.println("Path params: " + pathParams);

    if (!pathParams.isEmpty()) {
      System.out.println("Path params: " + pathParams.get(0));
      ResponseProtocol.sendSuccess(request, response, this, "Path params is not empty",
          Map.of("pathParams", pathParams), HttpServletResponse.SC_OK);
    } else {
      ResponseProtocol.sendError(request, response, this, "Path params is empty",
          Map.of("pathParams", new ArrayList<>()), HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Handles POST requests to create a resource.
   * 
   * @param request  the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws IOException if an input or output error is detected
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ TemplateServlet | doPost ------------");

    int userId = 0;
    try {
      requestBody.clear();
      requestBody.putAll(RequestProtocol.parseRequest(request));
      System.out.println("Request body: " + requestBody);

      sqlStatements = new String[] {
          "INSERT INTO Users (email, passwordHash, name, role, status) VALUES (?, ?, ?, 'Validator', 'inactive')",
          "UPDATE Users SET role = ? WHERE email = ?",
          "UPDATE Users SET name = ? WHERE email = ?",
          "SELECT userId FROM Users WHERE email = ?"

      };

      sqlParams.clear();

      sqlParams.add(new Object[] { "Test@test.com",
          "testPassword",
          "Test User" });

      sqlParams.add(new Object[] { "Admin", "Test@test.com" });

      sqlParams.add(new Object[] { "Edited Test User", "Test@test.com" });

      sqlParams.add(new Object[] { "Test@test.com" });

      results.clear();
      results.addAll(DatabaseActionUtils.executeSQL(sqlStatements, sqlParams));
      if (!results.isEmpty()) {
        System.out.println("After rs.next()");
        userId = (int) results.get(0).get("userId");
        System.out.println("User ID: " + userId);
      }

      sqlStatements = new String[] { "DELETE FROM Users WHERE userId = ?" };
      sqlParams.clear();
      sqlParams.add(new Object[] { userId });
      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      System.out.println("Test case done successfully");
      ResponseProtocol.sendSuccess(request, response, this, "Test case executed successfully",
          Map.of("userId", userId), HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error creating validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles PUT requests to update a resource.
   * 
   * @param request  the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws IOException if an input or output error is detected
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ TemplateServlet | doPut ------------");
    queryParams.clear();
    queryParams.putAll(RequestProtocol.parseQueryParams(request));
    System.out.println("Query params: " + queryParams);

    if (!queryParams.isEmpty()) {
      ResponseProtocol.sendSuccess(request, response, this, "Query params is not empty",
          Map.of("queryParams", queryParams), HttpServletResponse.SC_OK);
    } else {
      ResponseProtocol.sendError(request, response, this, "Query params is empty",
          Map.of("queryParams", new HashMap<>()), HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Handles DELETE requests to delete a resource.
   * 
   * @param request  the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws IOException if an input or output error is detected
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ TemplateServlet | doDelete ------------");
    pathParams.clear();
    pathParams.addAll(RequestProtocol.parsePathParams(request));
    System.out.println("Path params: " + pathParams);
  }
}