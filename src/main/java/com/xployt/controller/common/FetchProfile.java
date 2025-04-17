package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.SQLException;


import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
// import com.xployt.model.User;

/*
 * This is a template servlet for creating new servlets
 * It is used to be used to setup a new Controller
 * Try out theese ednpoints to se how it works
 */
@WebServlet("/api/fetch-profile/*")
public class FetchProfile extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  /**
   * Get a resource - Add a detailed entry
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ FetchProfile | doGet ------------");

    try {

      pathParams = RequestProtocol.parsePathParams(request);
      System.out.println("Path params: " + pathParams);

      if (pathParams != null && pathParams.size() == 1) {
        try {
          String userIdStr = pathParams.get(0);
          System.out.println("User ID string: " + userIdStr);
          
          if (userIdStr == null || userIdStr.trim().isEmpty()) {
            ResponseProtocol.sendError(
                request, 
                response, 
                this, 
                "Invalid user ID: empty value",
                Map.of("pathParams", pathParams),
                HttpServletResponse.SC_BAD_REQUEST
            );
            return;
          }
          
          int userId = Integer.parseInt(userIdStr.trim());

          System.out.println("Parsed User ID: " + userId);
          
          // First get the user's role
          sqlStatements = new String[] { "SELECT role FROM Users WHERE userId = ?" };
          sqlParams.clear();
          sqlParams.add(new Object[] { userId });
          
          results.clear();
          results.addAll(DatabaseActionUtils.executeSQL(sqlStatements, sqlParams));
          
          String userRole = "";
          if (!results.isEmpty()) {
              userRole = (String) results.get(0).get("role");
          }
          
          // Now fetch the full profile using the correct role
          sqlStatements = new String[] { getSQLForUserType(userRole) };
          sqlParams.clear();
          sqlParams.add(new Object[] { userId });
          
          results.clear();
          results.addAll(DatabaseActionUtils.executeSQL(sqlStatements, sqlParams));

          
          ResponseProtocol.sendSuccess(request, response, this, "Profile fetched successfully",
              Map.of("results", results),
              HttpServletResponse.SC_OK);
          return;
        } catch (NumberFormatException e) {
          System.out.println("Error: " + e);
          ResponseProtocol.sendError(request, response, this, "Error: " + e.getMessage(),
              Map.of("error", e.getMessage()), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      }
      System.out.println("Path params is empty");
      ResponseProtocol.sendError(request, response, this, "Path params is empty",
          Map.of("pathParams", new ArrayList<>()),
          HttpServletResponse.SC_BAD_REQUEST);

    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error: " + e.getMessage(),
          Map.of("error", e.getMessage()), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getSQLForUserType(String userType) {
    switch (userType) {
      case "Hacker":
      case "hacker":
        return "SELECT u.userId, u.email, u.name, u.role, u.createdAt, u.updatedAt, u.status, up.username, up.firstName, up.lastName, up.phone, up.companyName, up.dob, up.linkedIn, GROUP_CONCAT(hs.skill) as skills FROM Users u LEFT JOIN UserProfiles up ON u.userId = up.userId LEFT JOIN HackerSkillSet hs ON u.userId = hs.hackerId WHERE u.userId = ? GROUP BY u.userId";
      case "client":
      case "Client":
        return "SELECT u.userId, u.email, u.name, u.role, u.createdAt, u.updatedAt, u.status, up.username, up.firstName, up.lastName, up.phone, up.companyName, up.dob FROM Users u LEFT JOIN UserProfiles up ON u.userId = up.userId WHERE u.userId = ?";
      case "lead":
      case "ProjectLead":
        return "SELECT u.userId, u.email, u.name, u.role, u.createdAt, u.updatedAt, u.status, up.username, up.firstName, up.lastName, up.phone, up.companyName, up.dob, up.linkedIn, vi.skills, vi.experience, vi.cvLink, vi.reference, vi.activeProjectCount FROM Users u LEFT JOIN UserProfiles up ON u.userId = up.userId LEFT JOIN ValidatorInfo vi ON u.userId = vi.validatorId WHERE u.userId = ?";
      case "admin":
      case "Admin":
        return "SELECT u.userId, u.email, u.name, u.role, u.createdAt, u.updatedAt, u.status, up.username, up.firstName, up.lastName, up.phone, up.companyName, up.dob FROM Users u LEFT JOIN UserProfiles up ON u.userId = up.userId WHERE u.userId = ?";
      case "validator":
      case "Validator":
        return "SELECT u.userId, u.email, u.name, u.role, u.createdAt, u.updatedAt, u.status, up.username, up.firstName, up.lastName, up.phone, up.companyName, up.dob, up.linkedIn, vi.skills, vi.experience, vi.cvLink, vi.reference, vi.activeProjectCount FROM Users u LEFT JOIN UserProfiles up ON u.userId = up.userId LEFT JOIN ValidatorInfo vi ON u.userId = vi.validatorId WHERE u.userId = ?";
    }
    return null;
  }

}