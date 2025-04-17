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
import java.util.logging.Logger;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils; 
import com.xployt.util.CustomLogger;

@WebServlet("/api/new-profile/*")
public class NewProfileServlet extends HttpServlet {
    private static final Logger logger = CustomLogger.getLogger();
    private static String[] sqlStatements = {};
    private static List<Object[]> sqlParams = new ArrayList<>();
    private static List<Map<String, Object>> results = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ NewProfileServlet | doGet ------------");
        
        ArrayList<String> pathParams = RequestProtocol.parsePathParams(request);
        logger.info("Path params: " + pathParams);
        
        if (pathParams.isEmpty()) {
            ResponseProtocol.sendError(request, response, this, "User ID not provided", 
                Map.of("pathParams", new ArrayList<>()), 
                HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            int userId = Integer.parseInt(pathParams.get(0));
            logger.info("Fetching profile for userId: " + userId);
            
            // SQL to fetch profile
            sqlStatements = new String[] {
                "SELECT u.userId, u.name, u.email, u.role, up.phone, " +
                "up.username, up.firstName, up.lastName, up.companyName, " +
                "up.dob, up.linkedIn " +
                "FROM Users u " +
                "LEFT JOIN UserProfiles up ON u.userId = up.userId " +
                "WHERE u.userId = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (results.isEmpty() || results.get(0) == null) {
                ResponseProtocol.sendError(request, response, this, "Profile not found", 
                    Map.of("userId", userId), 
                    HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Map<String, Object> profileData = results.get(0);
            
            ResponseProtocol.sendSuccess(request, response, this, "Profile fetched successfully", 
                Map.of("profile", profileData), 
                HttpServletResponse.SC_OK);
            
        } catch (NumberFormatException e) {
            logger.severe("Invalid user ID format: " + pathParams.get(0) + ", Error: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Invalid user ID format", 
                e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Error fetching profile", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ NewProfileServlet | doPut ------------");
        
        ArrayList<String> pathParams = RequestProtocol.parsePathParams(request);
        logger.info("Path params: " + pathParams);
        
        if (pathParams.isEmpty()) {
            ResponseProtocol.sendError(request, response, this, "User ID not provided", 
                Map.of("pathParams", new ArrayList<>()), 
                HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            int userId = Integer.parseInt(pathParams.get(0));
            Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
            logger.info("Request body: " + requestBody);
            
            if (requestBody.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Request body is empty", 
                    Map.of(), 
                    HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // First check if user exists
            sqlStatements = new String[] {
                "SELECT userId FROM Users WHERE userId = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (results.isEmpty() || results.get(0) == null) {
                ResponseProtocol.sendError(request, response, this, "User not found", 
                    Map.of("userId", userId), 
                    HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Update Users table
            sqlStatements = new String[] {
                "UPDATE Users SET name = ?, email = ? WHERE userId = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { 
                requestBody.get("name"), 
                requestBody.get("email"), 
                userId 
            });
            
            DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            // Check if UserProfile exists
            sqlStatements = new String[] {
                "SELECT profileId FROM UserProfiles WHERE userId = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (!results.isEmpty() && results.get(0) != null) {
                // Update existing profile
                int profileId = (int) results.get(0).get("profileId");
                
                sqlStatements = new String[] {
                    "UPDATE UserProfiles SET " +
                    "phone = ?, username = ?, firstName = ?, " +
                    "lastName = ?, companyName = ?, dob = ?, " +
                    "linkedIn = ? WHERE profileId = ?"
                };
                
                sqlParams.clear();
                sqlParams.add(new Object[] { 
                    requestBody.get("phone"),
                    requestBody.get("username"),
                    requestBody.get("firstName"),
                    requestBody.get("lastName"),
                    requestBody.get("companyName"),
                    requestBody.get("dob") != null ? java.sql.Date.valueOf((String) requestBody.get("dob")) : null,
                    requestBody.get("linkedIn"),
                    profileId
                });
                
                DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            } else {
                // Insert new profile
                sqlStatements = new String[] {
                    "INSERT INTO UserProfiles (userId, phone, username, " +
                    "firstName, lastName, companyName, dob, linkedIn) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                };
                
                sqlParams.clear();
                sqlParams.add(new Object[] { 
                    userId,
                    requestBody.get("phone"),
                    requestBody.get("username"),
                    requestBody.get("firstName"),
                    requestBody.get("lastName"),
                    requestBody.get("companyName"),
                    requestBody.get("dob") != null ? java.sql.Date.valueOf((String) requestBody.get("dob")) : null,
                    requestBody.get("linkedIn")
                });
                
                DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            }
            
            ResponseProtocol.sendSuccess(request, response, this, "Profile updated successfully", 
                Map.of("userId", userId), 
                HttpServletResponse.SC_OK);
            
        } catch (NumberFormatException e) {
            logger.severe("Invalid user ID format: " + pathParams.get(0) + ", Error: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Invalid user ID format", 
                e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Error updating profile", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}