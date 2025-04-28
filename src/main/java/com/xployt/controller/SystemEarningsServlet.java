package com.xployt.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.service.SystemEarningsService;
import com.xployt.util.AuthUtil;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/system/earnings/*")
public class SystemEarningsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = CustomLogger.getLogger();
    private SystemEarningsService systemEarningsService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.systemEarningsService = new SystemEarningsService();
        logger.info("SystemEarningsServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            boolean isAdmin = AuthUtil.isAdmin(request);
            
            if (!isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can access system earnings");
                return;
            }
            
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Return all earnings
                List<Map<String, Object>> earnings = systemEarningsService.getAllEarnings();
                double totalEarnings = systemEarningsService.getTotalEarnings();
                
                Map<String, Object> result = new HashMap<>();
                result.put("earnings", earnings);
                result.put("totalEarnings", totalEarnings);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                GenericResponse genericResponse = new GenericResponse(result, true, "Success", null);
                response.getWriter().write(JsonUtil.toJson(genericResponse));
            } else if (pathInfo.equals("/daterange")) {
                // Get earnings by date range
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                
                if (startDate == null || endDate == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Start date and end date are required");
                    return;
                }
                
                List<Map<String, Object>> earnings = systemEarningsService.getEarningsByDateRange(startDate, endDate);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                GenericResponse genericResponse = new GenericResponse(earnings, true, "Success", null);
                response.getWriter().write(JsonUtil.toJson(genericResponse));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in SystemEarningsServlet: {0}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in SystemEarningsServlet: {0}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
} 