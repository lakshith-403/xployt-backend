package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.CustomLogger;

@WebServlet("/api/hackerSkills")
public class HackerSkillsServlet extends HttpServlet {
    private static final Logger logger = CustomLogger.getLogger();
    private static String[] sqlStatements = {};
    private static List<Object[]> sqlParams = new ArrayList<>();
    private static List<Map<String, Object>> results = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ HackerSkillsServlet | doGet ------------");
        
        try {
            // Fetch all skills from HackerSkills table
            sqlStatements = new String[] {
                "SELECT id, skill FROM HackerSkills ORDER BY skill"
            };
            
            sqlParams.clear();
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (results.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "No skills found", 
                    Map.of(), 
                    HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ResponseProtocol.sendSuccess(request, response, this, "Skills retrieved successfully", 
                Map.of("skills", results), 
                HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage() + " " + e.getStackTrace()[0]);
            ResponseProtocol.sendError(request, response, this, "Error retrieving skills", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
} 