package com.xployt.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.common.ProjectFinanceDAO;
import com.xployt.dao.common.SystemEarningsDAO;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;

public class ProjectFinanceService {
    private ProjectFinanceDAO projectFinanceDAO;
    private SystemEarningsDAO systemEarningsDAO;
    private FinanceService financeService;
    private static final Logger logger = CustomLogger.getLogger();
    private static final double COMMISSION_RATE = 0.20; // 20% commission

    public ProjectFinanceService() {
        this.projectFinanceDAO = new ProjectFinanceDAO();
        this.systemEarningsDAO = new SystemEarningsDAO();
        this.financeService = new FinanceService();
        logger.info("ProjectFinanceService initialized");
    }

    public List<Map<String, Object>> getProjectReportPayments(int projectId) throws SQLException {
        logger.log(Level.INFO, "Getting report payments for project ID: {0}", projectId);
        return projectFinanceDAO.getProjectReportPayments(projectId);
    }

    public boolean verifyProjectClient(int projectId, int clientId) throws SQLException {
        logger.log(Level.INFO, "Verifying client ID: {0} for project ID: {1}", new Object[]{clientId, projectId});
        return projectFinanceDAO.verifyProjectClient(projectId, clientId);
    }

    public GenericResponse processReportPayment(int reportId, int clientId) {
        try {
            logger.log(Level.INFO, "Processing payment for report ID: {0} by client ID: {1}", 
                new Object[]{reportId, clientId});
            
            Map<String, Object> reportPayment = projectFinanceDAO.getReportPaymentDetails(reportId);
            
            if (reportPayment == null) {
                return new GenericResponse(null, false, null, "Report not found");
            }
            
            Integer hackerId = (Integer) reportPayment.get("hackerId");
            Double paymentAmount = (Double) reportPayment.get("payment_amount");
            Boolean isPaid = (Boolean) reportPayment.get("paid");
            
            if (isPaid) {
                return new GenericResponse(null, false, null, "Report already paid");
            }

            if (hackerId == null || paymentAmount == null) {
                return new GenericResponse(null, false, null, 
                    "Invalid report data: missing hacker ID or payment amount");
            }
            
            // Calculate commission amount (20% of payment amount)
            Double commissionAmount = paymentAmount * COMMISSION_RATE;
            Double hackerAmount = paymentAmount - commissionAmount;
            
            // Client pays the full amount
            String description = "Payment for Bug Report ID: " + reportId;
            GenericResponse withdrawResponse = financeService.withdrawFunds(clientId, paymentAmount, description);
            
            if (!withdrawResponse.isIs_successful()) {
                return withdrawResponse;
            }
            
            // Hacker receives the payment minus commission
            GenericResponse depositResponse = financeService.addFunds(hackerId, hackerAmount, description);
            
            if (!depositResponse.isIs_successful()) {
                // Rollback the withdrawal
                financeService.addFunds(clientId, paymentAmount, "Refund for failed payment transaction");
                return depositResponse;
            }
            
            // Record commission in the system earnings
            String commissionDescription = "Commission for Bug Report ID: " + reportId;
            systemEarningsDAO.recordCommission(reportId, clientId, hackerId, commissionAmount, commissionDescription);
            
            // Record the payment
            projectFinanceDAO.recordPayment(reportId, hackerId, paymentAmount);
            
            Map<String, Object> updatedReport = projectFinanceDAO.getReportPaymentDetails(reportId);
            
            return new GenericResponse(updatedReport, true, "Payment processed successfully", null);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error processing report payment: {0}", e.getMessage());
            return new GenericResponse(null, false, null, "Error processing payment: " + e.getMessage());
        }
    }

    public Map<String, Object> getProjectFinanceDetails(int projectId) throws SQLException {
        logger.log(Level.INFO, "Getting project finance details for project ID: {0}", projectId);
        return projectFinanceDAO.getProjectFinanceDetails(projectId);
    }

    public Map<String, Object> getProjectReportPaymentsWithDetails(int projectId) throws SQLException {
        logger.log(Level.INFO, "Getting report payments with details for project ID: {0}", projectId);
        
        Map<String, Object> result = new HashMap<>();
        
        // Get reports payment data
        List<Map<String, Object>> reports = projectFinanceDAO.getProjectReportPayments(projectId);
        result.put("reports", reports);
        
        // Get project finance details
        Map<String, Object> projectDetails = getProjectFinanceDetails(projectId);
        
        // Add project details to the result
        result.put("projectId", projectId);
        if (projectDetails.containsKey("totalExpenditure")) {
            result.put("totalExpenditure", projectDetails.get("totalExpenditure"));
        } else {
            result.put("totalExpenditure", 0.0);
        }
        
        return result;
    }
} 