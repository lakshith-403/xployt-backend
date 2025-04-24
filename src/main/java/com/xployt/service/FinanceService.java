package com.xployt.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.common.FinanceDAO;
import com.xployt.dao.common.ProjectFinanceDAO;
import com.xployt.dao.common.UserDAO;
import com.xployt.model.FinanceTransaction;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;

public class FinanceService {
    private FinanceDAO financeDAO;
    private ProjectFinanceDAO projectFinanceDAO;
    private UserDAO userDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public FinanceService() {
        this.financeDAO = new FinanceDAO();
        this.projectFinanceDAO = new ProjectFinanceDAO();
        this.userDAO = new UserDAO();
    }

    public GenericResponse getUserBalance(int userId) {
        logger.log(Level.INFO, "Getting balance for user ID: {0}", userId);
        try {
            double balance = financeDAO.getUserBalance(userId);
            return new GenericResponse(balance, true, "Balance retrieved successfully", null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user balance: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error retrieving balance: " + e.getMessage());
        }
    }

    public GenericResponse addFunds(int userId, double amount, String description) {
        logger.log(Level.INFO, "Adding funds for user ID: {0}, amount: {1}, description: {2}", new Object[]{userId, amount, description});
        
        if (amount <= 0) {
            return new GenericResponse(null, false, null, "Amount must be greater than zero");
        }

        try {
            financeDAO.addFunds(userId, amount, description);
            double newBalance = financeDAO.getUserBalance(userId);
            return new GenericResponse(newBalance, true, "Funds added successfully", null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding funds: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error adding funds: " + e.getMessage());
        }
    }

    public GenericResponse withdrawFunds(int userId, double amount, String description) {
        if (amount <= 0) {
            return new GenericResponse(null, false, null, "Amount must be greater than zero");
        }

        try {
            financeDAO.withdrawFunds(userId, amount, description);
            double newBalance = financeDAO.getUserBalance(userId);
            return new GenericResponse(newBalance, true, "Funds withdrawn successfully", null);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Insufficient funds")) {
                return new GenericResponse(null, false, null, "Insufficient funds");
            }
            logger.log(Level.SEVERE, "Error withdrawing funds: {0}", errorMessage);
            return new GenericResponse(null, false, null, "Error withdrawing funds: " + errorMessage);
        }
    }

    public GenericResponse getUserTransactions(int userId) {
        try {
            List<FinanceTransaction> transactions = financeDAO.getUserTransactions(userId);
            return new GenericResponse(transactions, true, "Transactions retrieved successfully", null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user transactions: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error retrieving transactions: " + e.getMessage());
        }
    }

    public GenericResponse getUserFinanceSummary(int userId, String userRole) {
        logger.log(Level.INFO, "Getting finance summary for user ID: {0} with role: {1}", 
            new Object[]{userId, userRole});
        
        try {
            Map<String, Object> summary = new HashMap<>();
            double totalPaid = 0.0;
            double pendingPayments = 0.0;
            
            // Process depends on user role
            if (userRole.equals("Hacker")) {
                // For hackers, get the projects they are working on
                List<Map<String, Object>> hackerProjects = userDAO.getHackerProjects(userId);
                
                // For each project, get paid and validated-but-unpaid reports
                for (Map<String, Object> project : hackerProjects) {
                    int projectId = (Integer) project.get("projectId");
                    
                    // Get report payments for this project where this hacker is involved
                    List<Map<String, Object>> reports = projectFinanceDAO.getHackerReportPayments(projectId, userId);
                    
                    for (Map<String, Object> report : reports) {
                        boolean isPaid = (Boolean) report.get("paid");
                        String status = (String) report.get("status");
                        Double amount = (Double) report.get("payment_amount");
                        
                        if (amount != null) {
                            if (isPaid) {
                                totalPaid += amount;
                            } else if (status.equals("Validated")) {
                                pendingPayments += amount;
                            }
                        }
                    }
                }
            } else if (userRole.equals("Client")) {
                // For clients, get the projects they own
                List<Map<String, Object>> clientProjects = userDAO.getClientProjects(userId);
                
                // For each project, get payment information
                for (Map<String, Object> project : clientProjects) {
                    int projectId = (Integer) project.get("projectId");
                    
                    // Get all report payments for this project
                    List<Map<String, Object>> reports = projectFinanceDAO.getProjectReportPayments(projectId);
                    
                    for (Map<String, Object> report : reports) {
                        boolean isPaid = (Boolean) report.get("paid");
                        String status = (String) report.get("status");
                        Double amount = (Double) report.get("payment_amount");
                        
                        if (amount != null) {
                            if (isPaid) {
                                totalPaid += amount;
                            } else if (status.equals("Validated")) {
                                pendingPayments += amount;
                            }
                        }
                    }
                }
            }
            
            summary.put("totalPaid", totalPaid);
            summary.put("pendingPayments", pendingPayments);
            summary.put("totalAmount", totalPaid + pendingPayments);
            
            return new GenericResponse(summary, true, "Finance summary retrieved successfully", null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user finance summary: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error retrieving finance summary: " + e.getMessage());
        }
    }
} 