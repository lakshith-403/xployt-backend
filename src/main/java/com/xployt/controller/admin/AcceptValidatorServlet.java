package com.xployt.controller.admin;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xployt.util.ResponseProtocol;

import java.io.IOException;
import java.util.Map;
import com.xployt.util.RequestProtocol;
import java.util.ArrayList;
import java.util.List;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/admin/validatorApplications")
public class AcceptValidatorServlet extends HttpServlet {
  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ AcceptValidatorServlet | doGet ------------");
    try {
      Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      sqlStatements = new String[] {
          "SELECT * FROM Users WHERE status = 'inactive' AND role = 'Validator'"
      };

      sqlParams = new ArrayList<>();

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (results == null || results.isEmpty()) {
        ResponseProtocol.sendSuccess(request, response, this, "No validators found",
            Map.of("validators", results),
            HttpServletResponse.SC_OK);
        return;
      }

      ResponseProtocol.sendSuccess(request, response, this, "Validator accepted successfully",
          Map.of("validators", results),
          HttpServletResponse.SC_OK);

    } catch (Exception e) {
      System.out.println("Error parsing request: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error accepting validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ AcceptValidatorServlet | doPost ------------");

    int validatorId = 0;
    try {
      Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      sqlStatements = new String[] {
          "SELECT * FROM Users WHERE userId = ? AND role = 'Validator' AND status = 'inactive'"
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { requestBody.get("userId") });

      List<Map<String, Object>> rs = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator: " + rs);
      if (rs == null || rs.isEmpty()) {
        ResponseProtocol.sendError(request, response, this, "Validator not found", "Validator not found",
            HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      sqlStatements = new String[] {
          "UPDATE Users SET status = ? WHERE userId = ? AND role = 'Validator'"
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { requestBody.get("status"), requestBody.get("userId") });

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (requestBody.get("status").equals("active")) {
        System.out.println("Validator accepted successfully");

        ResponseProtocol.sendSuccess(request, response, this, "Validator accepted successfully",
            Map.of("validatorId", validatorId),
            HttpServletResponse.SC_CREATED);
      } else {
        System.out.println("Validator rejected successfully");
        ResponseProtocol.sendSuccess(request, response, this, "Validator rejected successfully",
            Map.of("validatorId", validatorId),
            HttpServletResponse.SC_CREATED);
      }

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error accepting validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ManageValidatorServlet | doPut ------------");
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ManageValidatorServlet | doDelete ------------");
  }

}