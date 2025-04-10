package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/common/projectFunding/*")
public class ProjectFundingServlet extends HttpServlet {

    /**
     * Servlet to handle project funding information
     * 
     * This servlet provides endpoints to retrieve  and manage project funding data
     * including initial funding and total expenditure for projects.
     * 
     * GET: Retrieves funding information for a specific project
     * URL pattern: /api/common/projectFunding/{projectId}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArrayList<String> pathParams = RequestProtocol.parsePathParams(request);
        
        if (pathParams.size() == 0) {
            ResponseProtocol.sendError(request, response, this, "Project ID is required",
                new HashMap<>(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String projectId = pathParams.get(0);
        try {
            String[] sqlStatements = {
                "SELECT initialFunding, totalExpenditure FROM ProjectConfigs WHERE projectId = ?"
            };
            List<Object[]> sqlParams = new ArrayList<>();
            sqlParams.add(new Object[] { projectId });
            
            List<Map<String, Object>> results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            Map<String, Object> response_data = new HashMap<>();
            if (!results.isEmpty()) {
                response_data.put("initialFunding", results.get(0).get("initialFunding"));
                response_data.put("totalExpenditure", results.get(0).get("totalExpenditure"));
            }

            ResponseProtocol.sendSuccess(request, response, this, "Project funding info fetched successfully",
                response_data, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            ResponseProtocol.sendError(request, response, this, "Error fetching project funding info",
                e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
} 