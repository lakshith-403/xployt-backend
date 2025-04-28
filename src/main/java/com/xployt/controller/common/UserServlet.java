package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/new-user/*")
public class UserServlet extends HttpServlet {

  private static final List<Object[]> sqlParams = new ArrayList<>();
  private static final List<Map<String, Object>> results = new ArrayList<>();
  private static final Map<String, Object> queryParams = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ UserServlet | doGet ------------");
    try {
      List<String> pathParams = RequestProtocol.parsePathParams(request);
      String roleId = pathParams.size() > 0 ? pathParams.get(0) : null;
      queryParams.putAll(RequestProtocol.parseQueryParams(request));
      String role = (String) queryParams.get("role");

      if (roleId != null && role != null) {
        String sqlStatement = getSQLForRole(role);
        sqlParams.clear();
        sqlParams.add(new Object[] { Integer.parseInt(roleId) });

        results.clear();
        results.addAll(DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, sqlParams));

        if (!results.isEmpty()) {
          ResponseProtocol.sendSuccess(request, response, this, "User data fetched successfully",
              Map.of("userData", results), HttpServletResponse.SC_OK);
        } else {
          ResponseProtocol.sendSuccess(request, response, this, "No user data found",
              Map.of("userData", new ArrayList<>()), HttpServletResponse.SC_NOT_FOUND);
        }
      } else {
        ResponseProtocol.sendError(request, response, this, "Invalid path params or role",
            Map.of("pathParams", pathParams, "role", role), HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error: " + e.getMessage(),
          Map.of("error", e.getMessage()), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getSQLForRole(String role) throws SQLException {
    switch (role) {
      case "client":
        return "SELECT * FROM Users WHERE userId = ?";
      case "lead":
        return "SELECT * FROM Users WHERE userId = ?";
      case "admin":
        return "SELECT * FROM Users WHERE userId = ?";
      case "validator":
        return "SELECT * FROM Users WHERE userId = ?";
      case "hacker":
        return "SELECT * FROM Users WHERE userId = ?";
      default:
        throw new SQLException("Invalid role");
    }
  }
}
