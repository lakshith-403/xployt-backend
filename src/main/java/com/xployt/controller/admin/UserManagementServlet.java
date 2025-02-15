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
  private static ArrayList<String> pathParams = new ArrayList<>();
  // private static Map<String, Object> requestBody = new HashMap<>();
  // private static Map<String, Object> queryParams = new HashMap<>();

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

      // Handle /api/admin/userManagement/{userType}
      if (pathParams.size() == 2) {
        String userType = pathParams.get(0);
        String userId = pathParams.get(1);

        switch (userType) {
          case "Validator":
            sqlStatements = new String[] {
                "SELECT * FROM Users WHERE role = 'Validator' AND userId = ?"
            };
            break;
          case "Admin":
            sqlStatements = new String[] {
                "SELECT * FROM Users WHERE role = 'Admin' AND userId = ?"
            };
            break;
          case "Hacker":
            sqlStatements = new String[] {
                "SELECT * FROM Users WHERE role = 'Hacker' AND userId = ?"
            };
            break;
          case "ProjectLead":
            sqlStatements = new String[] {
                "SELECT * FROM Users WHERE role = 'ProjectLead' AND userId = ?"
            };
            break;
          case "Client":
            sqlStatements = new String[] {
                "SELECT * FROM Users WHERE role = 'Client' AND userId = ?"
            };
            break;
          default:
            System.out.println("Invalid user type: " + userType);
            ResponseProtocol.sendError(request, response, this, "Invalid user type",
                "Invalid user type: " + userType,
                HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { userId });
        results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

        ResponseProtocol.sendSuccess(request, response, this, "User info fetched successfully",
            Map.of("users", results),
            HttpServletResponse.SC_OK);

        // Handle /api/admin/userManagement/{userType}/{userId}
      } else if (pathParams.size() == 1) {
        String userType = pathParams.get(0);
        sqlStatements = new String[] {
            "SELECT userId, name, email, status FROM Users WHERE role = ?"
        };

        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { userType });
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
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ UserManagementServlet | doPut ------------");
  }

  /*
   * Delete a user
   * Used to delete a user
   * Used in route: /admin/list-users
   * The reponse varies on whether a pathParam is set or not
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ UserManagementServlet | doDelete ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);
      System.out.println("Path params: " + pathParams);

      if (pathParams.size() == 1) {
        String userId = pathParams.get(0);
        sqlStatements = new String[] {
            "DELETE FROM UserProfiles WHERE userId = ?",
            "DELETE FROM Users WHERE userId = ?"
        };
        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { userId });
        sqlParams.add(new Object[] { userId });
        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

        System.out.println("User deleted successfully");
        ResponseProtocol.sendSuccess(request, response, this, "User deleted successfully",
            Map.of("userId", userId),
            HttpServletResponse.SC_OK);
      }
    } catch (Exception e) {
      System.out.println("Error deleting user: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error deleting user", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}