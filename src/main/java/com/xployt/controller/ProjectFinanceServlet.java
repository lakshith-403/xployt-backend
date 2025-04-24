package com.xployt.controller;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xployt.model.GenericResponse;
import com.xployt.service.FinanceService;
import com.xployt.service.ProjectFinanceService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/project-finance/*")
public class ProjectFinanceServlet extends HttpServlet {
    private ProjectFinanceService projectFinanceService;
    private FinanceService financeService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectFinanceService = new ProjectFinanceService();
        financeService = new FinanceService();
        logger.info("ProjectFinanceServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("ProjectFinanceServlet: GET request received");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        try {
            int projectId = Integer.parseInt(pathParts[1]);
            
            // Get payment information for all reports in a project with project details
            Map<String, Object> projectData = projectFinanceService.getProjectReportPaymentsWithDetails(projectId);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            GenericResponse genericResponse = new GenericResponse(projectData, true, "Success", null);
            response.getWriter().write(JsonUtil.toJson(genericResponse));
            
        } catch (NumberFormatException e) {
            logger.warning("ProjectFinanceServlet: Invalid project ID format: " + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        } catch (Exception e) {
            logger.severe("ProjectFinanceServlet: Error processing GET request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("ProjectFinanceServlet: POST request received");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3 || !pathParts[2].equals("pay")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        try {
            int projectId = Integer.parseInt(pathParts[1]);
            
            // Read request body
            String requestBody = request.getReader().lines()
                .collect(java.util.stream.Collectors.joining(System.lineSeparator()));
            
            if (requestBody.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }
            
            Gson gson = JsonUtil.useGson();
            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
            
            if (!jsonObject.has("reportId") || !jsonObject.has("clientId")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ReportId and clientId are required");
                return;
            }
            
            int reportId = jsonObject.get("reportId").getAsInt();
            int clientId = jsonObject.get("clientId").getAsInt();
            
            // Verify if the client is the owner of the project
            boolean isProjectClient = projectFinanceService.verifyProjectClient(projectId, clientId);
            if (!isProjectClient) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not the client of the project");
                return;
            }
            
            // Process payment
            GenericResponse result = projectFinanceService.processReportPayment(reportId, clientId);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(result));
            
        } catch (NumberFormatException e) {
            logger.warning("ProjectFinanceServlet: Invalid number format: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        } catch (Exception e) {
            logger.severe("ProjectFinanceServlet: Error processing POST request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
} 