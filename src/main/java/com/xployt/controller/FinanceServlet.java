package com.xployt.controller;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.xployt.model.GenericResponse;
import com.xployt.service.FinanceService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/finance/*")
public class FinanceServlet extends HttpServlet {
    private FinanceService financeService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        financeService = new FinanceService();
        logger.info("FinanceServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("FinanceServlet: GET request received");
        
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
            int userId = Integer.parseInt(pathParts[1]);
            
            GenericResponse result;
            if (pathParts.length == 2) {
                // Get balance: /api/finance/{userId}
                result = financeService.getUserBalance(userId);
            } else if (pathParts.length == 3 && pathParts[2].equals("transactions")) {
                // Get transactions: /api/finance/{userId}/transactions
                result = financeService.getUserTransactions(userId);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(result));
            
        } catch (NumberFormatException e) {
            logger.warning("FinanceServlet: Invalid user ID format: " + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            logger.severe("FinanceServlet: Error processing GET request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("FinanceServlet: POST request received");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[1]);
            String action = pathParts[2];
            
            // Read request body
            String requestBody = request.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));
            
            if (requestBody.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }
            
            JsonObject jsonObject = JsonUtil.fromJson(requestBody, JsonObject.class);
            if (!jsonObject.has("amount") || !jsonObject.has("description")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Amount and description are required");
                return;
            }
            
            double amount = jsonObject.get("amount").getAsDouble();
            String description = jsonObject.get("description").getAsString();
            
            GenericResponse result;
            if (action.equals("deposit")) {
                // Add funds: /api/finance/{userId}/deposit
                result = financeService.addFunds(userId, amount, description);
            } else if (action.equals("withdraw")) {
                // Withdraw funds: /api/finance/{userId}/withdraw
                result = financeService.withdrawFunds(userId, amount, description);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(result));
            
        } catch (NumberFormatException e) {
            logger.warning("FinanceServlet: Invalid number format: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        } catch (Exception e) {
            logger.severe("FinanceServlet: Error processing POST request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }
} 