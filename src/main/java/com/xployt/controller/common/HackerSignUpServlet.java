package com.xployt.controller.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.FileUploadUtil;
import com.xployt.util.FileUploadUtil.UploadResult;
import com.xployt.util.ResponseProtocol;

@WebServlet("/api/register")
@MultipartConfig
public class HackerSignUpServlet extends HttpServlet {
    private static final Logger logger = CustomLogger.getLogger();
    private static String[] sqlStatements = {};
    private static List<Object[]> sqlParams = new ArrayList<>();
    private static List<Map<String, Object>> results = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ HackerSignUpServlet | doPost ------------");
        
        try {
            // Process multipart request
            UploadResult uploadResult = FileUploadUtil.processMultipartRequest(request, response);
            if (uploadResult == null) {
                return; // Error already sent to client
            }
            
            Map<String, String> formFields = uploadResult.getFormFields();
            Map<String, Object> requestBody = new HashMap<>();
            
            // Extract form fields directly
            String email = formFields.get("email");
            String password = formFields.get("password");
            String username = formFields.get("username");
            String firstName = formFields.get("firstName");
            String lastName = formFields.get("lastName");
            String phone = formFields.get("phone");
            String companyName = formFields.get("companyName");
            String dob = formFields.get("dob");
            String linkedIn = formFields.get("linkedIn");
            
            // Convert form fields to requestBody map
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("username", username != null ? username : email); // Use email as username if not provided
            requestBody.put("firstName", firstName);
            requestBody.put("lastName", lastName);
            requestBody.put("phone", phone);
            requestBody.put("companyName", companyName);
            requestBody.put("dob", dob);
            requestBody.put("linkedIn", linkedIn);
            
            // Parse skills if provided
            String skillsStr = formFields.get("skills");
            List<String> skills = new ArrayList<>();
            if (skillsStr != null && !skillsStr.isEmpty()) {
                if (skillsStr.startsWith("[") && skillsStr.endsWith("]")) {
                    // It's a JSON array
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    skills = new Gson().fromJson(skillsStr, listType);
                } else {
                    // It's a comma-separated string
                    String[] skillsArray = skillsStr.split(",");
                    for (String skill : skillsArray) {
                        if (!skill.trim().isEmpty()) {
                            skills.add(skill.trim());
                        }
                    }
                }
            }
            requestBody.put("skills", skills);
            
            logger.info("Request body: " + requestBody);
            
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
                "Hacker" // Set role as Hacker
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
                "INSERT INTO UserProfiles (userId, username, firstName, lastName, phone, companyName, dob, linkedIn) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { 
                userId,
                username,
                firstName,
                lastName,
                phone,
                companyName,
                handleDateConversion(dob),
                linkedIn
            });
            
            DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

            // Process certificates if provided
            List<FileItem> certificateItems = new ArrayList<>();
            for (FileItem item : uploadResult.getFileItems()) {
                if ("certificates".equals(item.getFieldName())) {
                    certificateItems.add(item);
                }
            }
            
            if (!certificateItems.isEmpty()) {
                // Create list of certificate IDs (use userId + timestamps to ensure uniqueness)
                List<String> certificateIds = new ArrayList<>();
                for (int i = 0; i < certificateItems.size(); i++) {
                    certificateIds.add("cert_" + userId + "_" + System.currentTimeMillis() + "_" + i);
                }
                
                // Process certificate uploads
                List<File> uploadedCertificates = FileUploadUtil.processAttachments(
                    certificateItems,
                    certificateIds,
                    getServletContext(), 
                    response
                );
                
                // Save certificate information in database
                if (!uploadedCertificates.isEmpty()) {
                    sqlStatements = new String[] {
                        "INSERT INTO HackerCertificates (hackerId, certificatePath) VALUES (?, ?)"
                    };
                    
                    for (File certificate : uploadedCertificates) {
                        sqlParams.clear();
                        sqlParams.add(new Object[] { userId, certificate.getName() });
                        try {
                            DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                            logger.info("Added certificate: " + certificate.getName() + " for hacker: " + userId);
                        } catch (Exception e) {
                            logger.warning("Failed to add certificate: " + certificate.getName() + " for hacker: " + userId + " - " + e.getMessage());
                        }
                    }
                }
            } else {
                logger.info("No certificates provided for hacker: " + userId);
            }

            // Insert hacker skills if provided
            if (skills != null && !skills.isEmpty()) {
                sqlStatements = new String[] {
                    "INSERT INTO HackerSkillSet (hackerId, skill) VALUES (?, ?)"
                };
                
                for (String skill : skills) {
                    sqlParams.clear();
                    sqlParams.add(new Object[] { userId, skill });
                    try {
                        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                        logger.info("Added skill: " + skill + " for hacker: " + userId);
                    } catch (Exception e) {
                        logger.warning("Failed to add skill: " + skill + " for hacker: " + userId + " - " + e.getMessage());
                    }
                }
            } else {
                logger.info("No skills provided for hacker: " + userId);
            }

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