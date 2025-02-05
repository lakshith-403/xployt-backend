package com.xployt.controller.validator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xployt.util.ResponseProtocol;
import java.io.IOException;
import java.util.Map;
import com.xployt.util.RequestProtocol;
import java.util.ArrayList;
import java.util.List;
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

      sqlStatements = new String[] {
          "INSERT INTO UserProfiles (userId, firstName, lastName, phone, dob, linkedin) VALUES (?, ?, ?, ?, ?, ?)"
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { validatorId, firstName, lastName, phone, year + "-" + month + "-" + day, linkedin });

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator created successfully");

      ResponseProtocol.sendSuccess(request, response, this, "Validator created successfully",
          Map.of("validatorId", validatorId),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      if (validatorId != 0) {
        String[] sqlStatements = {
            "DELETE FROM Users WHERE userId = ?"
        };
        List<Object[]> sqlParams = new ArrayList<>();
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

}