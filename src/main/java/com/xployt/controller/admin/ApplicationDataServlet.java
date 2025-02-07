package com.xployt.controller.admin;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
// import java.util.HashMap;

import com.xployt.util.ResponseProtocol;
import com.xployt.util.RequestProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/admin/applicationData/*")
public class ApplicationDataServlet extends HttpServlet {

  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  // private static Map<String, Object> requestBody = new HashMap<>();
  // private static Map<String, Object> queryParams = new HashMap<>();

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ ApplicationDataServlet | doGet ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);
      // System.out.println("Request body: " + pathParams);

      String[] sqlStatements = {
          "SELECT * FROM UserProfiles WHERE userId = ? "
      };
      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { pathParams.get(0) });

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      if (results.size() > 0) {
        // System.out.println("Application Data: " + results);
        ResponseProtocol.sendSuccess(request, response, this, "Application Data fetched successfully",
            Map.of("applicationData", results),
            HttpServletResponse.SC_OK);
      }

    } catch (Exception e) {
      System.out.println("Error parsing request: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error: Error Message", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ ApplicationDataServlet | doPost ------------");
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ApplicationDataServlet | doPut ------------");
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ ApplicationDataServlet | doDelete ------------");
  }
}
