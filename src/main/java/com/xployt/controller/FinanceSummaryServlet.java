package com.xployt.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.service.FinanceService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/finance-summary/*")
public class FinanceSummaryServlet extends HttpServlet {
    private FinanceService financeService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        financeService = new FinanceService();
        logger.info("FinanceSummaryServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("FinanceSummaryServlet: GET request received");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path. Expected format: /userId/role");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[1]);
            String userRole = pathParts[2];
            
            // Validate user role
            if (!isValidRole(userRole)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user role: " + userRole);
                return;
            }

            GenericResponse result = financeService.getUserFinanceSummary(userId, userRole);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(result));
        } catch (NumberFormatException e) {
            logger.warning("FinanceSummaryServlet: Invalid user ID format: " + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            logger.severe("FinanceSummaryServlet: Error processing request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }
    
    private boolean isValidRole(String role) {
        return role.equals("Client") || role.equals("Hacker") || 
               role.equals("ProjectLead") || role.equals("Validator") || 
               role.equals("Admin");
    }
} 