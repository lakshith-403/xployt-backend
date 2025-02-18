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
// import com.xployt.model.User;

/*
 * This is a template servlet for creating new servlets
 * It is used to be used to setup a new Controller
 * Try out theese ednpoints to se how it works
 */
@WebServlet("/api/test/*")
public class TemplateServlet extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ TemplateServlet | doGet ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);

    if (pathParams.size() > 0) {
      System.out.println("Path params: " + pathParams.get(0));
      ResponseProtocol.sendSuccess(request, response, this, "Path params is not empty",
          Map.of("pathParams", pathParams),
          HttpServletResponse.SC_OK);
      return;
    }

    ResponseProtocol.sendError(request, response, this, "Path params is empty",
        Map.of("pathParams", new ArrayList<>()),
        HttpServletResponse.SC_BAD_REQUEST);

  }

  @Override

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ TemplateServlet | doPost ------------");

    int userId = 0;
    try {
      requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);
      // Simulate requestBody

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

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (results.size() > 0) {
        System.out.println("After rs.next()");
        userId = (int) results.get(0).get("userId");
        System.out.println("User ID: " + userId);
      }

      sqlStatements = new String[] {
          "DELETE FROM Users WHERE userId = ?"
      };
      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { userId });
      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      System.out.println("Test case done successfully");

      ResponseProtocol.sendSuccess(request, response, this, "Test case excuted successfully",
          Map.of("userId", userId),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error creating validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ TemplateServlet | doPut ------------");
    queryParams = RequestProtocol.parseQueryParams(request);
    System.out.println("Query params: " + queryParams);

    if (queryParams.size() > 0) {
      ResponseProtocol.sendSuccess(request, response, this, "Query params is not empty",
          Map.of("queryParams", queryParams),
          HttpServletResponse.SC_OK);
      return;
    }

    ResponseProtocol.sendError(request, response, this, "Query params is empty",
        Map.of("queryParams", new HashMap<>()),
        HttpServletResponse.SC_BAD_REQUEST);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ TemplateServlet | doDelete ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);
  }
}