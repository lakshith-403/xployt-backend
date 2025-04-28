package com.xployt.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xployt.dao.common.NotificationDAO;
import com.xployt.dao.common.UserDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.User;
import com.xployt.service.EmailService;
import com.xployt.service.FinanceService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/finance/*")
public class FinanceServlet extends HttpServlet {
    private FinanceService financeService;
    private EmailService emailService;
    private NotificationDAO notificationDAO;
    private UserDAO userDAO;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        financeService = new FinanceService();
        emailService = new EmailService();
        notificationDAO = new NotificationDAO();
        userDAO = new UserDAO();
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
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("userId", userId);
            responseMap.put("balance", result.getData());

            GenericResponse genericResponse = new GenericResponse(responseMap, true, "Success", null);
            response.getWriter().write(JsonUtil.toJson(genericResponse));
            
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
            
            Gson gson = JsonUtil.useGson();
            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
            System.out.println(jsonObject);
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
            } else if (action.equals("payment") && jsonObject.has("recipientId")) {
                // Payment to hacker: /api/finance/{userId}/payment
                int recipientId = jsonObject.get("recipientId").getAsInt();
                result = financeService.withdrawFunds(userId, amount, description);
                
                if (result.isIs_successful()) {
                    // Add funds to the recipient (hacker)
                    GenericResponse depositResult = financeService.addFunds(recipientId, amount, 
                            "Payment received: " + description);
                    
                    if (depositResult.isIs_successful()) {
                        // Send email notification to hacker
                        try {
                            User hacker = userDAO.getUserById(String.valueOf(recipientId));
                            if (hacker != null) {
                                // Send HTML email notification to hacker
                                double newBalance = (double) depositResult.getData();
                                emailService.sendPaymentNotification(
                                    hacker.getEmail(),
                                    hacker.getName(),
                                    amount,
                                    description,
                                    newBalance
                                );
                                
                                // Send in-app notification
                                notificationDAO.createPaymentNotification(
                                    String.valueOf(recipientId),
                                    amount,
                                    description,
                                    newBalance
                                );
                                
                                logger.info("Payment notification sent to hacker ID: " + recipientId);
                            }
                        } catch (Exception e) {
                            logger.warning("Failed to send notification: " + e.getMessage());
                            // Continue processing even if notification fails
                        }
                    }
                }
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