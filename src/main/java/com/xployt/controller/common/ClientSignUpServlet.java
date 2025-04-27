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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.CustomLogger;

@WebServlet("/api/clientRegister")
public class ClientSignUpServlet extends HttpServlet {
    private static final Logger logger = CustomLogger.getLogger();
    private static String[] sqlStatements = {};
    private static List<Object[]> sqlParams = new ArrayList<>();
    private static List<Map<String, Object>> results = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ ClientSignUpServlet | doPost ------------");
        
        try {
            Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
            logger.info("Request body: " + requestBody);
            
            if (requestBody.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Request body is empty", 
                    Map.of(), 
                    HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Validate required fields
            String email = (String) requestBody.get("email");
            String password = (String) requestBody.get("password");
            String firstName = (String) requestBody.get("firstName");
            String lastName = (String) requestBody.get("lastName");
            String phone = (String) requestBody.get("phone");
            String companyName = (String) requestBody.get("companyName");
            String dob = (String) requestBody.get("dob");
            String linkedIn = (String) requestBody.get("linkedIn");

            if (email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty() || 
                firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty() || 
                phone == null || phone.trim().isEmpty() || 
                dob == null || dob.trim().isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Missing required fields", 
                    Map.of("required", List.of("email", "password", "firstName", "lastName", "phone", "dob")), 
                    HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Check if email already exists
            sqlStatements = new String[] {
                "SELECT userId FROM Users WHERE email = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { email });
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (!results.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Email already registered", 
                    Map.of("email", email), 
                    HttpServletResponse.SC_CONFLICT);
                return;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);

            // Insert into Users table
            sqlStatements = new String[] {
                "INSERT INTO Users (email, passwordHash, name, role) VALUES (?, ?, ?, ?)",
                "SELECT LAST_INSERT_ID() as userId"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { 
                email,
                hashedPassword,
                firstName + " " + lastName, // Use full name as name
                "Client" // Set role as Client
            });
            sqlParams.add(new Object[] {}); // Empty params for SELECT query
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            if (results.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Failed to create user", 
                    Map.of(), 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Convert BigInteger to Integer for userId
            int userId = ((java.math.BigInteger) results.get(0).get("userId")).intValue();

            // Insert into UserProfiles table
            sqlStatements = new String[] {
                "INSERT INTO UserProfiles (userId, firstName, lastName, phone, companyName, dob, linkedIn) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { 
                userId,
                firstName,
                lastName,
                phone,
                companyName,
                handleDateConversion(dob),
                linkedIn
            });
            
            DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

            ResponseProtocol.sendSuccess(request, response, this, "Registration successful", 
                Map.of("userId", userId), 
                HttpServletResponse.SC_CREATED);
            
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage() + " " + e.getStackTrace()[0]);
            ResponseProtocol.sendError(request, response, this, "Error during registration", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Hashes a password using SHA-256
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        
        // Convert byte array to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Safely converts a date string to java.sql.Date
     */
    private java.sql.Date handleDateConversion(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        try {
            // Validate date format and range
            String[] parts = dateStr.split("-");
            if (parts.length != 3) {
                logger.warning("Invalid date format: " + dateStr);
                return null;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            // Validate year range (reasonable range: 1900-2100)
            if (year < 1900 || year > 2100) {
                logger.warning("Year out of valid range: " + year);
                return null;
            }

            // Validate month range
            if (month < 1 || month > 12) {
                logger.warning("Month out of valid range: " + month);
                return null;
            }

            // Validate day range
            if (day < 1 || day > 31) {
                logger.warning("Day out of valid range: " + day);
                return null;
            }

            // Format date string to ensure proper format
            String formattedDate = String.format("%04d-%02d-%02d", year, month, day);
            return java.sql.Date.valueOf(formattedDate);
        } catch (Exception e) {
            logger.warning("Failed to parse date: " + dateStr + ", Error: " + e.getMessage());
            return null;
        }
    }
} 