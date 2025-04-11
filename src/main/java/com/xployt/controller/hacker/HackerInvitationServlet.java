package com.xployt.controller.hacker;

import com.xployt.model.GenericResponse;
import com.xployt.model.Invitation;
import com.xployt.service.common.InvitationService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
// import java.sql.SQLException;
import com.xployt.util.DatabaseActionUtils;

import com.google.gson.Gson;
import com.xployt.util.CustomLogger;

@WebServlet("/api/invitations/hacker/*")

public class HackerInvitationServlet extends HttpServlet {
    private InvitationService invitationService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        invitationService = new InvitationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Servlet: Fetching hacker invitations");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path info");
            return;
        }
        String userId = pathInfo.substring(1);

        GenericResponse HackerInvitations;

        try {
            logger.info("Trying to fetch hacker invitations for user: " + userId);
            HackerInvitations = invitationService.fetchHackerInvitations(userId);
            logger.info("project invitations: " + HackerInvitations);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project invitations");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(HackerInvitations));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String requestBody = sb.toString();
        Gson gson = new Gson();
        return gson.fromJson(requestBody, Map.class);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Accepting Invitation");

        Map<String, Object> jsonObject;
        logger.info("Parsing request body");
        try {
            logger.info("Parsing request body");
            jsonObject = parseRequestBody(request);
            logger.info("jsonObject: " + jsonObject);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        String hackerId = jsonObject.get("hackerId") != null
                ? new Gson().toJson(jsonObject.get("hackerId")).replace(".0", "").replace("\"", "")
                : null;
        logger.info("hackerId: " + hackerId);
        String projectId = jsonObject.get("projectId") != null
                ? new Gson().toJson(jsonObject.get("projectId")).replace(".0", "").replace("\"", "")
                : null;
        logger.info("projectId: " + projectId);
        Boolean accepted = jsonObject.get("accept") != null
                ? new Gson().fromJson(jsonObject.get("accept").toString(), Boolean.class)
                : null;
        logger.info("accepted: " + accepted);

        if (hackerId == null || projectId == null || accepted == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        logger.info("Accepting invitation for projectId: " + projectId + ", hackerId: " + hackerId + ", accepted: " + accepted);

        Invitation invitation = new Invitation();
        invitation.setHackerId(hackerId);
        invitation.setProjectId(projectId);


        GenericResponse updatedInvitation;

        try {
            if (Boolean.TRUE.equals(accepted)) {
                invitation.setStatus("Accepted");
                updatedInvitation = invitationService.acceptInvitation(invitation);
            } else {
                invitation.setStatus("Declined");
                updatedInvitation = invitationService.rejectInvitation(invitation);
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error accepting invitation");
            return;
        }
        // If invitation was accepted, assign a suitable validator for the project and hacker
        if (Boolean.TRUE.equals(accepted)) {
            System.out.println("Finding suitable validator for project: " + projectId);
            
            try {
                // Get project scopes
                String scopeQuery = "SELECT ps.scopeId FROM ProjectScope ps WHERE ps.projectId = ?";
                List<Object[]> scopeParams = new ArrayList<>();
                scopeParams.add(new Object[] { projectId });
                List<Map<String, Object>> scopeResults = DatabaseActionUtils.executeSQL(new String[] { scopeQuery }, scopeParams);
                
                if (scopeResults.isEmpty()) {
                    System.out.println("No scopes found for project: " + projectId);
                } else {
                    // Extract scope IDs
                    List<Integer> scopeIds = new ArrayList<>();
                    for (Map<String, Object> scopeRow : scopeResults) {
                        scopeIds.add(Integer.parseInt(scopeRow.get("scopeId").toString()));
                    }
                    
                    System.out.println("Project scopes: " + scopeIds);
                    
                    // Get expertise IDs mapped to these scopes
                    String expertiseQuery = "SELECT se.skillId FROM scopeToExpertise se WHERE se.scopeId IN (" + 
                                         String.join(",", Collections.nCopies(scopeIds.size(), "?")) + ")";
                    List<Object[]> expertiseParams = new ArrayList<>();
                    expertiseParams.add(scopeIds.toArray());
                    List<Map<String, Object>> expertiseResults = DatabaseActionUtils.executeSQL(new String[] { expertiseQuery }, expertiseParams);
                    
                    if (expertiseResults.isEmpty()) {
                        System.out.println("No expertise mappings found for scopes");
                    } else {
                        // Extract expertise IDs
                        List<Integer> expertiseIds = new ArrayList<>();
                        for (Map<String, Object> expertiseRow : expertiseResults) {
                            expertiseIds.add(Integer.parseInt(expertiseRow.get("skillId").toString()));
                        }
                        
                        System.out.println("Required expertise IDs: " + expertiseIds);
                        
                        // Find validators with matching expertise
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < expertiseIds.size(); i++) {
                            inClause.append(expertiseIds.get(i));
                            if (i < expertiseIds.size() - 1) {
                                inClause.append(",");
                            }
                        }
                        
                        String validatorQuery = "SELECT u.userId AS userId, " +
                                             "COUNT(ves.skillId) AS matchCount, " +
                                             "COALESCE(vi.activeProjectCount, 0) AS activeCount " +
                                             "FROM Users u " +
                                             "LEFT JOIN ValidatorInfo vi ON u.userId = vi.validatorId " +
                                             "LEFT JOIN ValidatorExpertiseSet ves ON u.userId = ves.validatorId " +
                                             "AND ves.skillId IN (" + inClause.toString() + ") " +
                                             "WHERE u.role = 'Validator' AND u.status = 'active' " +
                                             "GROUP BY u.userId, vi.activeProjectCount " +
                                             "ORDER BY (0.7 * COUNT(ves.skillId) - 0.3 * COALESCE(vi.activeProjectCount, 0)) DESC " +
                                             "LIMIT 1";
                        
                        System.out.println("Executing validator selection query with expertise IDs: " + expertiseIds);
                        
                        // Execute the query with proper parameter handling
                        List<Object[]> validatorParams = new ArrayList<>();
                        validatorParams.add(new Object[0]); // Empty array since we've directly inserted the IDs in the query
                        List<Map<String, Object>> validatorResults = DatabaseActionUtils.executeSQL(
                            new String[] { validatorQuery },
                            validatorParams
                        );
                        if (validatorResults == null || validatorResults.isEmpty()) {
                            System.out.println("No suitable validator found with matching expertise");
                        } else {

                            Map<String, Object> validatorRow = validatorResults.get(0);
                            System.out.println("validatorResults: " + validatorRow);
                            int validatorId = Integer.parseInt(validatorRow.get("userId").toString());
                            int matchCount = Integer.parseInt(validatorRow.get("matchCount").toString());
                            
                            System.out.println("Selected validator ID: " + validatorId + " with " + matchCount + " matching expertise areas");
                            
                            // Update ProjectHackers record with the selected validator
                            String updateHackerQuery = "UPDATE ProjectHackers SET assignedValidatorId = ? " +
                                                      "WHERE projectId = ? AND hackerId = ?";
                            
                            // Add entry to ProjectValidators table
                            String insertValidatorQuery = "INSERT INTO ProjectValidators (projectId, validatorId) " +
                                                        "VALUES (?, ?)";

                            // Increase the activeProjectCount for the validator
                            String updateValidatorQuery = "UPDATE ValidatorInfo SET activeProjectCount = activeProjectCount + 1 " +
                                                         "WHERE validatorId = ?";
                            
                            
                            List<Object[]> sqlParams = new ArrayList<>();
                            sqlParams.add(new Object[] { validatorId, projectId, hackerId });
                            sqlParams.add(new Object[] { projectId, validatorId });
                            sqlParams.add(new Object[] { validatorId });
                            
                            DatabaseActionUtils.executeSQL(
                                new String[] { updateHackerQuery, insertValidatorQuery, updateValidatorQuery }, 
                                sqlParams
                            );
                            
                            System.out.println("Validator " + validatorId + " assigned to project " + projectId + " for hacker " + hackerId);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error assigning validator: " + e);
                // Rollback any changes made in the try block
                if (projectId != null && hackerId != null) {
                    try {                  
                        // Check if a record exists in ProjectHackers and delete it
                        String deleteHackerQuery = "DELETE FROM ProjectHackers " +
                                                  "WHERE projectId = ? AND hackerId = ?";
                        
                        // Update invitation status to Pending
                        String updateInvitationQuery = "UPDATE Invitations SET Status = 'Pending' " +
                                                      "WHERE HackerID = ? AND ProjectID = ?";
                        
                        List<Object[]> sqlParams = new ArrayList<>();
                        sqlParams.add(new Object[] { projectId, hackerId });
                        sqlParams.add(new Object[] { hackerId, projectId });
                        
                        DatabaseActionUtils.executeSQL(
                            new String[] { deleteHackerQuery, updateInvitationQuery }, 
                            sqlParams
                        );
                        
                        System.out.println("Reset invitation status to Pending for project " + projectId + " and hacker " + hackerId);
                        // Check if a validator was assigned
                        String checkValidatorQuery = "SELECT validatorId FROM ProjectValidators " +
                                                    "WHERE projectId = ?";
                        
                        List<Object[]> checkParams = new ArrayList<>();
                        checkParams.add(new Object[] { projectId });
                        
                        List<Map<String, Object>> checkResults = DatabaseActionUtils.executeSQL(
                            new String[] { checkValidatorQuery }, 
                            checkParams
                        );
                        
                        if (!checkResults.isEmpty() && checkResults.get(0).get("validatorId") != null) {
                            int validatorId = Integer.parseInt(checkResults.get(0).get("validatorId").toString());

                            // Decrease the activeProjectCount for the validator
                            String updateValidatorQuery = "UPDATE ValidatorInfo SET activeProjectCount = GREATEST(activeProjectCount - 1, 0) " +
                                                         "WHERE validatorId = ?";
                            
                            // Remove entry from ProjectValidators table
                            String deleteValidatorQuery = "DELETE FROM ProjectValidators " +
                                                         "WHERE projectId = ? AND validatorId = ?";
                            
                            List<Object[]> rollbackParams = new ArrayList<>();
                            rollbackParams.add(new Object[] { projectId, hackerId });
                            rollbackParams.add(new Object[] { validatorId });
                            rollbackParams.add(new Object[] { projectId, validatorId });
                            
                            DatabaseActionUtils.executeSQL(
                                new String[] {updateValidatorQuery, deleteValidatorQuery }, 
                                rollbackParams
                            );
             
                            System.out.println("Rolled back validator assignment for project " + projectId + " and hacker " + hackerId);
                        }
                    } catch (Exception rollbackEx) {
                        System.out.println("Error during rollback: " + rollbackEx.getMessage());
                    }
                }
                // Continue processing as this is not critical for the invitation acceptance
            }
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(updatedInvitation));
    }
}