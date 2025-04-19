package com.xployt.service;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.common.FinanceDAO;
import com.xployt.model.FinanceTransaction;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;

public class FinanceService {
    private FinanceDAO financeDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public FinanceService() {
        this.financeDAO = new FinanceDAO();
    }

    public GenericResponse getUserBalance(int userId) {
        try {
            double balance = financeDAO.getUserBalance(userId);
            return new GenericResponse(balance, true, "Balance retrieved successfully", null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user balance: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error retrieving balance: " + e.getMessage());
        }
    }

    public GenericResponse addFunds(int userId, double amount, String description) {
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
} 