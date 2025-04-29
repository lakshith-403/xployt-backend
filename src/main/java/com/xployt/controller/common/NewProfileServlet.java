package com.xployt.controller.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.FileUploadUtil;
import com.xployt.util.FileUploadUtil.UploadResult;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol; 

@WebServlet("/api/new-profile/*")
@MultipartConfig
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
            // Fetch all users with role-specific data
            fetchAllUsers(request, response);
            return;
        }
        
        try {
            int userId = Integer.parseInt(pathParams.get(0));
            logger.info("Fetching profile for userId: " + userId);
            
            // SQL to fetch profile
            sqlStatements = new String[] {
                "SELECT u.userId, u.name, u.email, u.role, up.phone, " +
                "up.username, up.firstName, up.lastName, up.companyName, " +
                "up.dob, up.linkedIn, up.profilePicture " +
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
            
            // Debug logging to check profile picture
            if (profileData.containsKey("profilePicture")) {
                logger.info("Profile picture value: " + profileData.get("profilePicture"));
            } else {
                logger.info("No profile picture found in query results");
            }
            
            // Fetch role-specific data
            String role = (String) profileData.get("role");
            if (role != null) {
                fetchRoleSpecificData(userId, role, profileData);
            }
            
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
    
    /**
     * Fetches all users with their common and role-specific data
     */
    private void fetchAllUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Fetching all users with role-specific data");
        
        try {
            // SQL to fetch all users with their common data
            sqlStatements = new String[] {
                "SELECT u.userId, u.name, u.email, u.role, up.phone, " +
                "up.username, up.firstName, up.lastName, up.companyName, " +
                "up.dob, up.linkedIn, up.profilePicture " +
                "FROM Users u " +
                "LEFT JOIN UserProfiles up ON u.userId = up.userId"
            };
            
            sqlParams.clear();
            
            List<Map<String, Object>> allUsers = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            // Populate role-specific data for each user
            for (Map<String, Object> user : allUsers) {
                int userId = (int) user.get("userId");
                String role = (String) user.get("role");
                
                if (role != null) {
                    fetchRoleSpecificData(userId, role, user);
                }
            }
            
            ResponseProtocol.sendSuccess(request, response, this, "All profiles fetched successfully", 
                Map.of("profiles", allUsers), 
                HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            logger.severe("Error fetching all users: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Error fetching all profiles", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Fetches role-specific data for a user and adds it to their profile data
     */
    private void fetchRoleSpecificData(int userId, String role, Map<String, Object> profileData) {
        logger.info("Fetching role-specific data for userId: " + userId + ", role: " + role);
        
        try {
            switch (role) {
                case "Validator":
                case "ProjectLead":
                    // Fetch ValidatorInfo for Validators and Project Leads
                    sqlStatements = new String[] {
                        "SELECT skills, experience, cvLink, reference " +
                        "FROM ValidatorInfo " +
                        "WHERE validatorId = ?"
                    };
                    
                    sqlParams.clear();
                    sqlParams.add(new Object[] { userId });
                    
                    List<Map<String, Object>> validatorResults = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    
                    if (!validatorResults.isEmpty() && validatorResults.get(0) != null) {
                        Map<String, Object> validatorInfo = validatorResults.get(0);
                        profileData.put("skills", validatorInfo.get("skills"));
                        profileData.put("experience", validatorInfo.get("experience"));
                        profileData.put("cvLink", validatorInfo.get("cvLink"));
                        profileData.put("reference", validatorInfo.get("reference"));
                    }
                    break;
                    
                case "Hacker":
                    logger.info("Processing Hacker role for userId: " + userId);
                    
                    // Fetch HackerSkillSet for Hackers
                    sqlStatements = new String[] {
                        "SELECT skill FROM HackerSkillSet WHERE hackerId = ?"
                    };
                    
                    sqlParams.clear();
                    sqlParams.add(new Object[] { userId });
                    
                    List<Map<String, Object>> hackerResults = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    
                    if (!hackerResults.isEmpty()) {
                        List<String> skills = new ArrayList<>();
                        for (Map<String, Object> skillRow : hackerResults) {
                            skills.add((String) skillRow.get("skill"));
                        }
                        profileData.put("skillSet", skills);
                    }

                    // Fetch Blast Points for Hackers
                    logger.info("Fetching blast points for userId: " + userId);
                    String blastPointsQuery = "SELECT points FROM HackerBlastPoints WHERE userId = ?";
                    logger.info("Blast points query: " + blastPointsQuery);
                    
                    sqlStatements = new String[] { blastPointsQuery };
                    sqlParams.clear();
                    sqlParams.add(new Object[] { userId });
                    
                    logger.info("Executing blast points query with params: " + userId);
                    List<Map<String, Object>> blastPointsResults = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    logger.info("Blast points query results: " + blastPointsResults);
                    
                    if (!blastPointsResults.isEmpty() && blastPointsResults.get(0) != null) {
                        Map<String, Object> blastPointsData = blastPointsResults.get(0);
                        Object points = blastPointsData.get("points");
                        if (points != null) {
                            profileData.put("blastPoints", points);
                            logger.info("Added blast points: " + points + " for userId: " + userId);
                        } else {
                            profileData.put("blastPoints", 0);
                            logger.info("No blast points found, setting to 0 for userId: " + userId);
                        }
                    } else {
                        profileData.put("blastPoints", 0);
                        logger.info("No blast points record found, setting to 0 for userId: " + userId);
                    }
                    break;
                    
                default:
                    // No role-specific data needed for other roles
                    break;
            }
        } catch (Exception e) {
            logger.severe("Error fetching role-specific data: " + e.getMessage());
            logger.severe("Stack trace: " + e.getStackTrace()[0]);
            // We don't want to fail the entire request if role-specific data fails
            // So just log the error and continue
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("\n------------ NewProfileServlet | doPost ------------");
        
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
            
            // Process multipart request
            UploadResult uploadResult = FileUploadUtil.processMultipartRequest(request, response);
            if (uploadResult == null) {
                return; // Error already sent to client
            }
            
            Map<String, String> formFields = uploadResult.getFormFields();
            Map<String, Object> requestBody = new HashMap<>();

            System.out.println("Form fields: " + formFields);
            
            // Extract profile data from form fields
            String profileJson = formFields.get("profile");
            if (profileJson != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> profileData = mapper.readValue(profileJson, Map.class);
                    
                    // Extract fields from profile data
                    String email = (String) profileData.get("email");
                    String name = (String) profileData.get("name");
                    String username = (String) profileData.get("username");
                    String firstName = (String) profileData.get("firstName");
                    String lastName = (String) profileData.get("lastName");
                    String phone = (String) profileData.get("phone");
                    String companyName = (String) profileData.get("companyName");
                    String dob = (String) profileData.get("dob");
                    String linkedIn = (String) profileData.get("linkedIn");
                    
                    // Convert form fields to requestBody map
                    requestBody.put("email", email);
                    requestBody.put("name", name);
                    requestBody.put("username", username);
                    requestBody.put("firstName", firstName);
                    requestBody.put("lastName", lastName);
                    requestBody.put("phone", phone);
                    requestBody.put("companyName", companyName);
                    requestBody.put("dob", dob);
                    requestBody.put("linkedIn", linkedIn);
                    
                    // Add skillSet if present
                    if (profileData.containsKey("skillSet")) {
                        requestBody.put("skillSet", profileData.get("skillSet"));
                    }
                } catch (Exception e) {
                    logger.severe("Error parsing profile JSON: " + e.getMessage());
                    ResponseProtocol.sendError(request, response, this, "Invalid profile data format", 
                        e.getMessage(), 
                        HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            
            if (requestBody.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Request body is empty", 
                    Map.of(), 
                    HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // First check if user exists and get role
            sqlStatements = new String[] {
                "SELECT userId, role, name, email FROM Users WHERE userId = ?"
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
            
            String role = (String) results.get(0).get("role");
            System.out.println("Tryna update users with");
            System.out.println(requestBody);
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
            
            // Check if UserProfile exists and get current values
            sqlStatements = new String[] {
                "SELECT * FROM UserProfiles WHERE userId = ?"
            };
            
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            
            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            
            Map<String, Object> currentProfile = null;
            
            if (!results.isEmpty() && results.get(0) != null) {
                // Update existing profile
                currentProfile = results.get(0);
                
                // Preserve existing values if not in request
                if (!requestBody.containsKey("phone") && currentProfile.containsKey("phone")) {
                    requestBody.put("phone", currentProfile.get("phone"));
                }
                
                if (!requestBody.containsKey("username") && currentProfile.containsKey("username")) {
                    requestBody.put("username", currentProfile.get("username"));
                }
                
                if (!requestBody.containsKey("firstName") && currentProfile.containsKey("firstName")) {
                    requestBody.put("firstName", currentProfile.get("firstName"));
                }
                
                if (!requestBody.containsKey("lastName") && currentProfile.containsKey("lastName")) {
                    requestBody.put("lastName", currentProfile.get("lastName"));
                }
                
                if (!requestBody.containsKey("companyName") && currentProfile.containsKey("companyName")) {
                    requestBody.put("companyName", currentProfile.get("companyName"));
                }
                
                if (!requestBody.containsKey("dob") && currentProfile.containsKey("dob")) {
                    requestBody.put("dob", currentProfile.get("dob"));
                }
                
                if (!requestBody.containsKey("linkedIn") && currentProfile.containsKey("linkedIn")) {
                    requestBody.put("linkedIn", currentProfile.get("linkedIn"));
                }
                
                // Preserve the existing profilePicture value when updating
                if (!requestBody.containsKey("profilePicture") && currentProfile.containsKey("profilePicture")) {
                    requestBody.put("profilePicture", currentProfile.get("profilePicture"));
                }
                
                // Log what we're trying to update
                logger.info("Updating profile with values: " + requestBody);
                
                sqlStatements = new String[] {
                    "UPDATE UserProfiles SET " +
                    "username = ?, firstName = ?, lastName = ?, " +
                    "phone = ?, companyName = ?, dob = ?, " +
                    "linkedIn = ?, profilePicture = ? WHERE userId = ?"
                };
                
                sqlParams.clear();
                sqlParams.add(new Object[] { 
                    requestBody.get("username"),
                    requestBody.get("firstName"),
                    requestBody.get("lastName"),
                    requestBody.get("phone"),
                    requestBody.get("companyName"),
                    requestBody.get("dob") != null ? handleDateConversion(requestBody.get("dob")) : null,
                    requestBody.get("linkedIn"),
                    requestBody.get("profilePicture"),
                    userId
                });
                
                DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            } else {
                // Insert new profile
                sqlStatements = new String[] {
                    "INSERT INTO UserProfiles (userId, username, firstName, lastName, " +
                    "phone, companyName, dob, linkedIn, profilePicture) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                };
                
                sqlParams.clear();
                sqlParams.add(new Object[] { 
                    userId,
                    requestBody.get("username"),
                    requestBody.get("firstName"),
                    requestBody.get("lastName"),
                    requestBody.get("phone"),
                    requestBody.get("companyName"),
                    requestBody.get("dob") != null ? handleDateConversion(requestBody.get("dob")) : null,
                    requestBody.get("linkedIn"),
                    null  // Default null for profilePicture initially
                });
                
                DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
            }
            
            // Update role-specific data
            updateRoleSpecificData(userId, role, requestBody);
            
            // Handle profile picture upload if present
            List<FileItem> profilePictureItems = new ArrayList<>();
            for (FileItem item : uploadResult.getFileItems()) {
                if ("profilePicture".equals(item.getFieldName())) {
                    profilePictureItems.add(item);
                    break; // Just take the first one if multiple are somehow sent
                }
            }
            
            if (!profilePictureItems.isEmpty()) {
                FileItem profilePictureItem = profilePictureItems.get(0);
                String originalFilename = profilePictureItem.getName();
                String fileExtension = "";
                int lastDot = originalFilename.lastIndexOf('.');
                if (lastDot > 0) {
                    fileExtension = originalFilename.substring(lastDot);
                }
                
                // Create a unique filename
                String profilePicFilename = "profile_" + userId + "_" + System.currentTimeMillis() + fileExtension;
                
                // Get upload directory
                String uploadPath = getServletContext().getRealPath("/uploads");
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                logger.info("Upload directory: " + uploadPath);
                
                // Save the file
                File profilePictureFile = new File(uploadDir, profilePicFilename);
                logger.info("Saving profile picture to: " + profilePictureFile.getAbsolutePath());
                try (InputStream inputStream = profilePictureItem.getInputStream()) {
                    java.nio.file.Files.copy(
                        inputStream, 
                        profilePictureFile.toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    
                    logger.info("Saved profile picture: " + profilePicFilename + " for user: " + userId);
                    
                    // Update the profile picture path in database
                    sqlStatements = new String[] {
                        "UPDATE UserProfiles SET profilePicture = ? WHERE userId = ?"
                    };
                    
                    sqlParams.clear();
                    sqlParams.add(new Object[] { profilePicFilename, userId });
                    
                    DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    
                } catch (Exception e) {
                    logger.severe("Error saving profile picture: " + e.getMessage());
                    // Don't fail the entire request if profile picture upload fails
                }
            }
            
            // Handle certificates for hackers
            if ("Hacker".equals(role)) {
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
            logger.severe("Error processing request: " + e.getMessage() + " " + e.getStackTrace()[0]);
            ResponseProtocol.sendError(request, response, this, "Error updating profile", 
                e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Updates role-specific data for a user
     */
    private void updateRoleSpecificData(int userId, String role, Map<String, Object> requestBody) {
        logger.info("Updating role-specific data for userId: " + userId + ", role: " + role);
        
        try {
            switch (role) {
                case "Validator":
                case "ProjectLead":
                    // Check if ValidatorInfo exists
                    sqlStatements = new String[] {
                        "SELECT validatorId FROM ValidatorInfo WHERE validatorId = ?"
                    };
                    
                    sqlParams.clear();
                    sqlParams.add(new Object[] { userId });
                    
                    results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    
                    if (!results.isEmpty() && results.get(0) != null) {
                        // Update existing ValidatorInfo
                        sqlStatements = new String[] {
                            "UPDATE ValidatorInfo SET " +
                            "skills = ?, experience = ?, cvLink = ?, reference = ? " +
                            "WHERE validatorId = ?"
                        };
                        
                        sqlParams.clear();
                        sqlParams.add(new Object[] { 
                            requestBody.get("skills"),
                            requestBody.get("experience"),
                            requestBody.get("cvLink"),
                            requestBody.get("reference"),
                            userId
                        });
                        
                        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    } else {
                        // Insert new ValidatorInfo
                        sqlStatements = new String[] {
                            "INSERT INTO ValidatorInfo (validatorId, skills, experience, cvLink, reference) " +
                            "VALUES (?, ?, ?, ?, ?)"
                        };
                        
                        sqlParams.clear();
                        sqlParams.add(new Object[] { 
                            userId,
                            requestBody.get("skills"),
                            requestBody.get("experience"),
                            requestBody.get("cvLink"),
                            requestBody.get("reference")
                        });
                        
                        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                    }
                    break;
                    
                case "Hacker":
                    // Handle Hacker skill set update
                    if (requestBody.containsKey("skillSet")) {
                        Object skillSetObj = requestBody.get("skillSet");
                        logger.info("Received skillSet object: " + skillSetObj);
                        
                        if (skillSetObj instanceof List) {
                            List<?> skillSetList = (List<?>) skillSetObj;
                            logger.info("Processing " + skillSetList.size() + " skills");
                            
                            // Delete existing skills first
                            sqlStatements = new String[] {
                                "DELETE FROM HackerSkillSet WHERE hackerId = ?"
                            };
                            
                            sqlParams.clear();
                            sqlParams.add(new Object[] { userId });
                            DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                            logger.info("Deleted existing skills for hackerId: " + userId);
                            
                            // Insert new skills
                            int skillsAdded = 0;
                            for (Object skillObj : skillSetList) {
                                if (skillObj instanceof String) {
                                    String skill = (String) skillObj;
                                    if (!skill.trim().isEmpty()) {
                                        sqlStatements = new String[] {
                                            "INSERT INTO HackerSkillSet (hackerId, skill) VALUES (?, ?)"
                                        };
                                        
                                        sqlParams.clear();
                                        sqlParams.add(new Object[] { userId, skill });
                                        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
                                        skillsAdded++;
                                    }
                                }
                            }
                            logger.info("Added " + skillsAdded + " new skills for hackerId: " + userId);
                        }
                    }
                    break;
                    
                default:
                    // No role-specific data to update for other roles
                    break;
            }
        } catch (Exception e) {
            logger.severe("Error updating role-specific data: " + e.getMessage());
            // We don't want to fail the entire request if role-specific data update fails
            // So just log the error and continue
        }
    }

    /**
     * Safely converts a date object or string to java.sql.Date
     */
    private java.sql.Date handleDateConversion(Object dateObj) {
        if (dateObj == null) return null;
        
        logger.info("Attempting to convert date object: " + dateObj + " of type: " + dateObj.getClass().getName());
        
        try {
            if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                if (dateStr.trim().isEmpty()) return null;
                
                // Try to parse the date in format YYYY-MM-DD
                try {
                    return java.sql.Date.valueOf(dateStr);
                } catch (IllegalArgumentException e) {
                    logger.warning("Failed to parse date using direct valueOf: " + dateStr + ", Error: " + e.getMessage());
                    
                    // Try with different formats
                    try {
                        // Try to parse with java.text.SimpleDateFormat
                        java.text.SimpleDateFormat sdf;
                        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                            sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        } else if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}.*")) {
                            sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
                        } else {
                            sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        }
                        
                        java.util.Date parsedDate = sdf.parse(dateStr);
                        return new java.sql.Date(parsedDate.getTime());
                    } catch (Exception ex) {
                        logger.warning("Failed to parse date using SimpleDateFormat: " + dateStr + ", Error: " + ex.getMessage());
                        
                        // Last resort: try to extract date components manually
                        if (dateStr.matches(".*\\d{4}.*")) {
                            try {
                                // Extract year, month, day with regex
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})");
                                java.util.regex.Matcher matcher = pattern.matcher(dateStr);
                                
                                if (matcher.find()) {
                                    int year = Integer.parseInt(matcher.group(1));
                                    int month = Integer.parseInt(matcher.group(2));
                                    int day = Integer.parseInt(matcher.group(3));
                                    
                                    // Validate date components
                                    if (year >= 1900 && year <= 2100 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                                        return java.sql.Date.valueOf(String.format("%04d-%02d-%02d", year, month, day));
                                    }
                                }
                            } catch (Exception extractionEx) {
                                logger.warning("Failed to extract date components: " + dateStr);
                            }
                        }
                        
                        // If all parsing attempts fail, return null
                        return null;
                    }
                }
            } else if (dateObj instanceof java.util.Date) {
                // If it's already a Date object, convert to sql.Date
                return new java.sql.Date(((java.util.Date) dateObj).getTime());
            } else if (dateObj instanceof Long) {
                // If it's a timestamp
                return new java.sql.Date((Long) dateObj);
            } else if (dateObj instanceof java.sql.Date) {
                // If it's already a sql.Date, return it directly
                return (java.sql.Date) dateObj;
            } else {
                logger.warning("Unexpected date format: " + dateObj.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            logger.warning("Failed to convert date: " + dateObj + ", Error: " + e.getMessage());
            return null;
        }
    }
}