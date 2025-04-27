package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.RequestProtocol;

@WebServlet("/api/project/fetch/*")
public class ProjectFetchServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      if (!RequestProtocol.authenticateRequest(request, response)) {
        return;
      }
      // Retrieve the projectId from the query parameters
      String projectIdParam = (String) RequestProtocol.parsePathParams(request).get(0);
      if (projectIdParam == null || projectIdParam.isEmpty()) {
        ResponseProtocol.sendError(request, response, this, "Missing projectId",
            "Missing projectId", HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      int projectId = Integer.parseInt(projectIdParam);

      // 1. Fetch Payment Level Amounts
      String paymentAmountsSql = "SELECT projectId, level, amount FROM PaymentLevelAmounts WHERE projectId = ?";
      List<Object[]> paymentAmountsParams = new ArrayList<>();
      paymentAmountsParams.add(new Object[] { projectId });
      List<Map<String, Object>> paymentAmounts = DatabaseActionUtils.executeSQL(
          new String[] { paymentAmountsSql }, paymentAmountsParams);

      // 2. Fetch Payment Levels
      String paymentLevelsSql = "SELECT projectId, level, item FROM PaymentLevels WHERE projectId = ?";
      List<Object[]> paymentLevelsParams = new ArrayList<>();
      paymentLevelsParams.add(new Object[] { projectId });
      List<Map<String, Object>> paymentLevels = DatabaseActionUtils.executeSQL(
          new String[] { paymentLevelsSql }, paymentLevelsParams);

      // 3. Fetch Scope Information (scopeId and scopeName)
      String scopeSql = "SELECT ps.scopeId, si.scopeName FROM ProjectScope ps " +
          "JOIN scopeItems si ON ps.scopeId = si.scopeId " +
          "WHERE ps.projectId = ?";
      List<Object[]> scopeParams = new ArrayList<>();
      scopeParams.add(new Object[] { projectId });
      List<Map<String, Object>> scopes = DatabaseActionUtils.executeSQL(
          new String[] { scopeSql }, scopeParams);

      // Combine all the results into a single response map
      Map<String, Object> result = new HashMap<>();
      result.put("paymentAmounts", paymentAmounts);
      result.put("paymentLevels", paymentLevels);
      result.put("scopes", scopes);

      ResponseProtocol.sendSuccess(request, response, this, "Fetched project information successfully",
          result, HttpServletResponse.SC_OK);

    } catch (SQLException e) {
      ResponseProtocol.sendError(request, response, this, "Error fetching project info",
          e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      ResponseProtocol.sendError(request, response, this, "Unexpected error",
          e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
