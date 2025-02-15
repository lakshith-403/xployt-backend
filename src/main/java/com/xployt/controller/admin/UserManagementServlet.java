package com.xployt.controller.admin;

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

@WebServlet("/api/admin/userManagement/*")
public class UserManagementServlet extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  /*
   * Get all users
   * Used to view user info
   * Depending on user type the fetched info can vary
   * Used in route: /admin/list/users
   * The reponse varies on whether a pathParam is set or not
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ UserManagementServlet | doGet ------------");
    try {

      pathParams = RequestProtocol.parsePathParams(request);
      System.out.println("Path params: " + pathParams);

      // Handle /api/admin/userManagement/{userId}
      if (pathParams.size() > 0) {
        System.out.println("Path params: " + pathParams.get(0));
        String userType = pathParams.get(0);
        String userId = pathParams.get(1);

        switch (userType) {
          case "Validator":
            sqlStatements = new String[] {
                "SELECT * FROM Users INNER JOIN UserProfiles ON Users.userId = UserProfiles.userId WHERE Users.userId = ?"
            };
            break;
          case "Admin":
          case "Hacker":
          case "ProjectLead":
          case "Client":
            sqlStatements = new String[] {
                "SELECT * FROM Users INNER JOIN UserProfiles ON Users.userId = UserProfiles.userId WHERE Users.userId = ?"
            };
            break;
        }
        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { userId });
        results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

        ResponseProtocol.sendSuccess(request, response, this, "User fetched successfully",
            Map.of("user", results),
            HttpServletResponse.SC_OK);

      } else {
        // Handle /api/admin/userManagement
        sqlStatements = new String[] {
            "SELECT userId, name, role, email, status FROM Users"
        };

        sqlParams = new ArrayList<>();
        results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

        if (results.size() > 0) {
          System.out.println("Users fetched successfully");
          ResponseProtocol.sendSuccess(request, response, this, "Users fetched successfully",
              Map.of("users", results),
              HttpServletResponse.SC_OK);
        } else {
          System.out.println("No users found");
          ResponseProtocol.sendSuccess(request, response, this, "No users found",
              Map.of("users", results),
              HttpServletResponse.SC_OK);
        }
      }
    } catch (Exception e) {
      System.out.println("Error fetching users: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error fetching users", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ UserManagementServlet | doPost ------------");

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

      sqlParams = new ArrayList<>();

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
    System.out.println("\n------------ UserManagementServlet | doPut ------------");
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
    System.out.println("\n------------ UserManagementServlet | doDelete ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);
  }

  // @Override
  // protected void doPut(HttpServletRequest request, HttpServletResponse
  // response)
  // throws IOException {
  // System.out.println("\n------------ TemplateServlet | doPut 2 ------------");

  // User user = RequestProtocol.parseRequest(request, User.class);
  // System.out.println("User: " + user);

  // requestBody = RequestProtocol.parseRequest(request);
  // System.out.println("Request body: " + requestBody);

  // ResponseProtocol.sendSuccess(request, response, this, "User updated
  // successfully",
  // Map.of("user", user),
  // HttpServletResponse.SC_OK);
  // }

}