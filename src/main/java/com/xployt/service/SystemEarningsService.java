package com.xployt.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.xployt.dao.common.SystemEarningsDAO;
import com.xployt.util.CustomLogger;

public class SystemEarningsService {
    private SystemEarningsDAO systemEarningsDAO;
    private static final Logger logger = CustomLogger.getLogger();
    
    public SystemEarningsService() {
        this.systemEarningsDAO = new SystemEarningsDAO();
        logger.info("SystemEarningsService initialized");
    }
    
    public List<Map<String, Object>> getAllEarnings() throws SQLException {
        return systemEarningsDAO.getAllEarnings();
    }
    
    public double getTotalEarnings() throws SQLException {
        return systemEarningsDAO.getTotalEarnings();
    }
    
    public List<Map<String, Object>> getEarningsByDateRange(String startDate, String endDate) throws SQLException {
        return systemEarningsDAO.getEarningsByDateRange(startDate, endDate);
    }
} 