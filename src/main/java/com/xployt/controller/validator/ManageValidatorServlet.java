package com.xployt.controller.validator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.sql.SQLException;
// import java.util.Arrays;

import com.xployt.util.ResponseProtocol;
import com.xployt.util.RequestProtocol;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.PasswordUtil;

@WebServlet("/api/validator/manage")
public class ManageValidatorServlet extends HttpServlet {

  /*
   * Create a new validator when a validator applicatin is submitted
   * Used by: Guest
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ ManageValidatorServlet | doPost ------------");

    int validatorId = 0;
    try {
      Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
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

      String[] sqlStatements = {
          "INSERT INTO Users (email, passwordHash, name, role, status) VALUES (?, ?, ?, 'Validator', 'inactive')",
          "SELECT userId FROM Users WHERE email = ?"
      };

      List<Object[]> sqlParams = new ArrayList<>();

      sqlParams.add(new Object[] { requestBody.get("email"),
          PasswordUtil.hashPassword("password"),
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
      // String address = (String) requestBody.get("address");
      String linkedin = (String) requestBody.get("linkedin");
      String skills = (String) requestBody.get("skills");
      String relevantExperience = (String) requestBody.get("relevantExperience");
      String references = (String) requestBody.get("references");

      sqlStatements = new String[] {
          "INSERT INTO UserProfiles (userId, firstName, lastName, phone, dob, linkedin) VALUES (?, ?, ?, ?, ?, ?)",
          "INSERT INTO ValidatorInfo (validatorId, skills, experience, reference) VALUES (?, ?, ?, ?)"
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { validatorId, firstName, lastName, phone, year + "-" + month + "-" + day, linkedin });
      sqlParams.add(new Object[] { validatorId, skills, relevantExperience, references });

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator created successfully");



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
            "DELETE FROM ValidatorSkillSet WHERE validatorId = ?",
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
    for (Map<String, Object> result : results) {
        expertiseIdMap.put((String) result.get("expertiseName"), (Integer) result.get("expertiseId"));
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
}
