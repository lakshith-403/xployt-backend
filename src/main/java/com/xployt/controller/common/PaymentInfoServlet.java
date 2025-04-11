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

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
// import com.xployt.model.User;

/*
 * This 
 */
@WebServlet("/api/common/paymentInfo/*")
public class PaymentInfoServlet extends HttpServlet {

  // private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  // private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, String[]> queryParams = new HashMap<>();

  /**
   * Get payment information for a project based on user role
   * 
   * For ProjectLead, Client, and Admin roles: Returns all payment levels and amounts
   * For other roles (like Hackers): Returns payment levels with report counts for the specific user
   * 
   * URL: /api/common/paymentInfo/{projectId}?role={role}&userId={userId}
   * 
   * Path parameters:
   * - projectId: ID of the project to get payment information for
   * 
   * Query parameters:
   * - role: User role (ProjectLead, Client, Admin, or other)
   * - userId: Required for non-admin roles to filter reports by user
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ PaymentInfoServlet | doGet ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    queryParams = request.getParameterMap();
    
    if (pathParams.size() == 0) {
      ResponseProtocol.sendError(request, response, this, "Project ID is required",
          new HashMap<>(), HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String projectId = pathParams.get(0);
    String role = queryParams.get("role")[0];
    
    try {
      String sql;
      if (role.equals("ProjectLead") || role.equals("Client") || role.equals("Admin")) {
        sql = "SELECT level, amount, reportCount FROM PaymentLevelAmounts WHERE projectId = ? ORDER BY amount DESC";
      } else {
        sql = "SELECT p.level, p.amount, " +
              "COUNT(CASE WHEN b.status = 'Validated' THEN 1 END) as reportCount " +
              "FROM PaymentLevelAmounts p " +
              "LEFT JOIN BugReports b ON b.projectId = p.projectId " +
              "AND UPPER(b.severity) = p.level " +
              "AND b.hackerId = ? " +
              "WHERE p.projectId = ? " +
              "GROUP BY p.level, p.amount " +
              "ORDER BY p.amount DESC";
      }

      sqlParams.clear();
      if (role.equals("ProjectLead") || role.equals("Client") || role.equals("Admin")) {
        sqlParams.add(new Object[] { projectId });
      } else {
        String userId = queryParams.get("userId")[0];
        sqlParams.add(new Object[] { userId, projectId });
      }

      results = DatabaseActionUtils.executeSQL(new String[] { sql }, sqlParams);
      
      Map<String, Object> response_data = new HashMap<>();
      response_data.put("payments", results);

      ResponseProtocol.sendSuccess(request, response, this, "Payment info fetched successfully",
          response_data, HttpServletResponse.SC_OK);

    } catch (Exception e) {
      System.out.println("Error fetching payment info: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error fetching payment info",
          e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}