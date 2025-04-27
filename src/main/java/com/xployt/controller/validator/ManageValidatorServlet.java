package com.xployt.controller.validator;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xployt.model.Attachment;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.FileUploadUtil;
import com.xployt.util.JsonUtil;
import com.xployt.util.PasswordUtil;
import com.xployt.util.ResponseProtocol;

@WebServlet("/api/validator/manage")
public class ManageValidatorServlet extends HttpServlet {
  private static final SecureRandom random = new SecureRandom();
  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

  /*
   * Create a new validator when a validator applicatin is submitted
   * Used by: Guest
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ ManageValidatorServlet | doPost ------------");

    int validatorId = 0;
    try {
      FileUploadUtil.UploadResult uploadResult = FileUploadUtil.processMultipartRequest(request, response);
        if (uploadResult == null) {
            System.err.println("Upload result is null");
            return;
        }

        // Check if the request contains the "application" form field
     Gson gson = JsonUtil.useGson();
     String requestBodyJson = uploadResult.getFormField("application");
     Map<String, Object> requestBody = gson.fromJson(requestBodyJson, new TypeToken<Map<String, Object>>() {}.getType());
     System.out.println("Request body: " + requestBody);

      // Extract areaOfExpertise from request body
      List<String> expertiseAreas = new ArrayList<>();
      Object areaOfExpertiseObj = requestBody.get("areaOfExpertise");

      if (areaOfExpertiseObj instanceof Map) {
        Map<?, ?> expertiseMap = (Map<?, ?>) areaOfExpertiseObj;
        expertiseAreas = expertiseMap.values()
                                   .stream()
                                   .map(Object::toString)
                                   .collect(Collectors.toList());
        System.out.println("Expertise Areas: " + expertiseAreas);
      }

      // Generate a secure random password for initial setup
      String temporaryPassword = generateRandomPassword(12);
      
      String[] sqlStatements = {
          "INSERT INTO Users (email, passwordHash, name, role, status) VALUES (?, ?, ?, 'Validator', 'inactive')",
          "SELECT userId FROM Users WHERE email = ?"
      };

      List<Object[]> sqlParams = new ArrayList<>();

      sqlParams.add(new Object[] { requestBody.get("email"),
          PasswordUtil.hashPassword(temporaryPassword),
          requestBody.get("name") });

      sqlParams.add(new Object[] { requestBody.get("email") });

      List<Map<String, Object>> rs = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (rs.size() > 0) {
        System.out.println("After rs.next()");
        validatorId = (int) rs.get(0).get("userId");
        System.out.println("Validator ID: " + validatorId);
      }

      String firstName = ((String) requestBody.get("name")).split(" ")[0];
      String lastName = ((String) requestBody.get("name")).split(" ")[1];
      System.out.println("First Name: " + firstName);
      System.out.println("Last Name: " + lastName);

      String phone = (String) requestBody.get("mobile");
      System.out.println("Phone: " + phone);
      // Extracting dateOfBirth
      Object dobObject = requestBody.get("dateOfBirth");
      String day = "", month = "", year = "";

      if (dobObject instanceof Map) {
        Map<?, ?> dobMap = (Map<?, ?>) dobObject;
        if (dobMap.containsKey("day") && dobMap.containsKey("month") && dobMap.containsKey("year")) {

          day = (String) dobMap.get("day");
          month = (String) dobMap.get("month");
          year = (String) dobMap.get("year");
          System.out.println("Date of Birth: " + day + "-" + month + "-" + year);

        } else {
          throw new IllegalArgumentException("Invalid dateOfBirth format");
        }
      } else {
        throw new IllegalArgumentException("dateOfBirth is not a valid Map");
      }

        // Extract file data
        List<Attachment> fileData = extractAttachments(requestBody, "cvProcessed");
        System.out.println("FileData: " + fileData);

        // List<String> fleIDs = fileData.stream().map(Attachment::getId).collect(Collectors.toList());

// //        upload files
//         List<File> uploadedFiles = FileUploadUtil.processAttachments(
//                 uploadResult.getFileItems(),
//                 fleIDs,
//                 getServletContext(),
//                 response
//         );


      // String address = (String) requestBody.get("address");
      String linkedin = (String) requestBody.get("linkedin");
      String skills = (String) requestBody.get("skills");
      String relevantExperience = (String) requestBody.get("relevantExperience");
      String references = (String) requestBody.get("references");

      sqlStatements = new String[] {
          "INSERT INTO UserProfiles (userId, firstName, lastName, phone, dob, linkedin) VALUES (?, ?, ?, ?, ?, ?)",
          "INSERT INTO ValidatorInfo (validatorId, skills, experience, reference, cvId) VALUES (?, ?, ?, ?, ?)",
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { validatorId, firstName, lastName, phone, year + "-" + month + "-" + day, linkedin });
      
      // Process CV file attachment
      String cvId = null;
      if (!fileData.isEmpty()) {
          cvId = fileData.get(0).getId();
      }
      
      sqlParams.add(new Object[] { validatorId, skills, relevantExperience, references, cvId });

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator created successfully");

      // Process attachments - first save them to the Attachment table
      if (!fileData.isEmpty()) {
          List<Object[]> attachmentBatchParams = new ArrayList<>();
          for (Attachment attachment : fileData) {
              attachmentBatchParams.add(new Object[] { attachment.getId(), attachment.getName(), attachment.getUrl() });
          }
          String attachmentSQL = "INSERT INTO Attachment (id, name, url) VALUES (?, ?, ?)";
          DatabaseActionUtils.executeBatchSQL(attachmentSQL, attachmentBatchParams);
          
          System.out.println("Saved " + fileData.size() + " attachments to database");
      }

      // After creating the validator and ValidatorInfo entry, insert expertise areas
      if (!expertiseAreas.isEmpty()) {
        insertValidatorExpertise(validatorId, expertiseAreas);
      }

      ResponseProtocol.sendSuccess(request, response, this, "Validator created successfully",
          Map.of("validatorId", validatorId),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      if (validatorId != 0) {
        String[] sqlStatements = {
            "DELETE FROM Users WHERE userId = ?",
            "DELETE FROM UserProfiles WHERE userId = ?",
            "DELETE FROM ValidatorInfo WHERE validatorId = ?",
            "DELETE FROM ValidatorExpertiseSet WHERE validatorId = ?",
        };
        List<Object[]> sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { validatorId });
        sqlParams.add(new Object[] { validatorId });
        sqlParams.add(new Object[] { validatorId });
        sqlParams.add(new Object[] { validatorId });
        try {
          DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
        } catch (Exception e1) {
          System.out.println("Error deleting validator: " + e1.getMessage());
        }
      }

      ResponseProtocol.sendError(request, response, this, "Error creating validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ManageValidatorServlet | doPut ------------");
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ManageValidatorServlet | doDelete ------------");
  }

  // Add this method to get expertise IDs
  private Map<String, Integer> getExpertiseIds(List<String> expertiseNames) throws SQLException {
    if (expertiseNames.isEmpty()) {
        return Collections.emptyMap();
    }
    
    String placeholders = expertiseNames.stream()
        .map(s -> "?")
        .collect(Collectors.joining(","));
        
    String selectSql = "SELECT expertiseName, expertiseId FROM ValidatorExpertise WHERE expertiseName IN (" + placeholders + ")";

    List<Object[]> selectParams = new ArrayList<>();
    selectParams.add(expertiseNames.toArray());

    List<Map<String, Object>> results = DatabaseActionUtils.executeSQL(
        new String[] { selectSql }, selectParams);
        
    Map<String, Integer> expertiseIdMap = new HashMap<>();
    if (!results.isEmpty()) {
        // The results from executeSQL are already a List<Map<String, Object>>
        // No need to cast results.get(0) to a List
        for (Map<String, Object> row : results) {
            expertiseIdMap.put((String) row.get("expertiseName"), (Integer) row.get("expertiseId"));
        }
    }
    return expertiseIdMap;
  }

  private void insertValidatorExpertise(int validatorId, List<String> expertiseNames) throws SQLException {
    if (expertiseNames.isEmpty()) {
        return;
    }

    // Get expertise IDs for the provided names
    Map<String, Integer> expertiseIdMap = getExpertiseIds(expertiseNames);
    
    // Prepare batch parameters for ValidatorExpertiseSet insertion
    List<Object[]> expertiseParams = new ArrayList<>();
    for (String expertiseName : expertiseNames) {
        Integer expertiseId = expertiseIdMap.get(expertiseName);
        if (expertiseId != null) {
            expertiseParams.add(new Object[] { validatorId, expertiseId });
        }
    }

    // Insert into ValidatorExpertiseSet table
    if (!expertiseParams.isEmpty()) {
        String insertSql = "INSERT INTO ValidatorExpertiseSet (validatorId, skillId) VALUES (?, ?)";
        DatabaseActionUtils.executeBatchSQL(insertSql, expertiseParams);
    }
  }

  /**
   * Generates a random password with the specified length
   * @param length The length of the password to generate
   * @return A randomly generated password
   */
  private String generateRandomPassword(int length) {
    StringBuilder password = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return password.toString();
  }

  private List<Attachment> extractAttachments(Map<String, Object> requestBody, String... keys) {
        List<Attachment> attachments = new ArrayList<>();
        System.out.println("Extracting attachments for keys: " + String.join(", ", keys));

        for (String key : keys) {
            Object attachmentObject = requestBody.get(key);
            System.out.println("Processing key: " + key + ", value: " + attachmentObject);

            if (attachmentObject == null) {
                System.out.println("No attachment found for key: " + key);
                continue;
            }

            // Handle case where attachmentObject is directly a Map (single attachment)
            if (attachmentObject instanceof Map<?, ?>) {
                Map<?, ?> itemMap = (Map<?, ?>) attachmentObject;
                String id = itemMap.get("id") != null ? itemMap.get("id").toString() : null;
                String name = itemMap.get("name") != null ? itemMap.get("name").toString() : null;
                String url = itemMap.get("url") != null ? itemMap.get("url").toString() : null;
                
                System.out.println("Parsed attachment data - id: " + id + ", name: " + name + ", url: " + url);
                if (id != null && name != null && url != null) {
                    attachments.add(new Attachment(id, name, url));
                    System.out.println("Added single attachment from map: " + itemMap);
                }
            } 
            // Handle case where attachmentObject is a List of attachments
            else if (attachmentObject instanceof List<?>) {
                List<?> attachmentList = (List<?>) attachmentObject;
                System.out.println("Processing attachment list for key: " + key);
                for (Object item : attachmentList) {
                    if (item instanceof Map<?, ?>) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        String id = itemMap.get("id") != null ? itemMap.get("id").toString() : null;
                        String name = itemMap.get("name") != null ? itemMap.get("name").toString() : null;
                        String url = itemMap.get("url") != null ? itemMap.get("url").toString() : null;
                        
                        System.out.println("Parsed attachment data - id: " + id + ", name: " + name + ", url: " + url);
                        if (id != null && name != null && url != null) {
                            attachments.add(new Attachment(id, name, url));
                            System.out.println("Added attachment from list item: " + itemMap);
                        } else {
                            System.err.println("Invalid attachment data: " + item);
                        }
                    } else {
                        System.err.println("Invalid attachment format: " + item);
                    }
                }
            } else {
                System.err.println("Invalid format for key: " + key + ", value: " + attachmentObject);
            }
        }

        System.out.println("Extracted attachments: " + attachments);
        return attachments;
    }
}
