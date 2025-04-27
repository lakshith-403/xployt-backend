package com.xployt.controller.admin;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.service.EmailService;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.PasswordUtil;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;

@WebServlet("/api/admin/validatorApplications")
public class AcceptValidatorServlet extends HttpServlet {
  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static final SecureRandom random = new SecureRandom();
  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

  /*
   * AcceptValidatorServlet : doGet
   * Fetches all validators who have fetches applications but have not been
   * processed (Accepted or rejected)
   * 
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ AcceptValidatorServlet | doGet ------------");
    try {
      if (!RequestProtocol.authorizeRequest(request, response, new String[] { "Admin" })) {
        return;
      }

      sqlStatements = new String[] {
          "SELECT * FROM Users WHERE status = 'inactive' AND role = 'Validator'"
      };

      sqlParams.clear();

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      // System.out.println("Results: " + results);
      if (results == null || results.isEmpty()) {
        System.out.println("No validators found");
        ResponseProtocol.sendSuccess(request, response, this, "No validators found",
            Map.of("validators", results),
            HttpServletResponse.SC_OK);
        return;
      }
      System.out.println("Validators found");
      ResponseProtocol.sendSuccess(request, response, this, "Validators fetched successfully",
          Map.of("validators", results),
          HttpServletResponse.SC_OK);

    } catch (Exception e) {
      System.out.println("Error parsing request: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error accepting validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  /*
   * AcceptValidatorServlet : doPost
   * Accepts or rejects a validator application
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ AcceptValidatorServlet | doPost ------------");
    if (!RequestProtocol.authorizeRequest(request, response, new String[] { "Admin" })) {
      return;
    }


    int validatorId = 0;
    try {
      Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      sqlStatements = new String[] {
          "SELECT * FROM Users WHERE userId = ? AND role = 'Validator' AND status = 'inactive'"
      };

      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { requestBody.get("userId") });

      List<Map<String, Object>> rs = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator: " + rs);
      if (rs == null || rs.isEmpty()) {
        ResponseProtocol.sendError(request, response, this, "Validator not found", "Validator not found",
            HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String validatorEmail = (String) rs.get(0).get("email");
      validatorId = (int) rs.get(0).get("userId");
      String validatorName = (String) rs.get(0).get("name");

      if (requestBody.get("status").equals("active")) {
        // Generate a random password for the validator
        String generatedPassword = generateRandomPassword(12);
        String passwordHash = PasswordUtil.hashPassword(generatedPassword);
        
        // Update validator status and password hash
        sqlStatements = new String[] {
            "UPDATE Users SET status = ?, passwordHash = ? WHERE userId = ? AND role = 'Validator'"
        };

        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { requestBody.get("status"), passwordHash, requestBody.get("userId") });

        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
        System.out.println("Validator accepted successfully");
        
        // Send email to the validator
        String emailSubject = "Xployt Validator Application Accepted";
        String emailContent = "Dear " + validatorName + ",\n\n" +
                              "Congratulations! Your application to become a validator at Xployt has been accepted.\n\n" +
                              "You can now log in to your validator account using the following credentials:\n" +
                              "Email: " + validatorEmail + "\n" +
                              "Temporary Password: " + generatedPassword + "\n\n" +
                              "Please change your password immediately after logging in for security reasons.\n\n" +
                              "Thank you for joining Xployt as a validator!\n\n" +
                              "Best regards,\n" +
                              "The Xployt Team";
        
        // Send email to validator
        EmailService emailService = new EmailService();
        emailService.sendEmail(validatorEmail, emailSubject, emailContent);

        ResponseProtocol.sendSuccess(request, response, this, "Validator accepted successfully",
            Map.of("validatorId", validatorId),
            HttpServletResponse.SC_CREATED);
      } else {
        sqlStatements = new String[] {
            "UPDATE Users SET status = ? WHERE userId = ? AND role = 'Validator'"
        };

        sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { requestBody.get("status"), requestBody.get("userId") });

        DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
        System.out.println("Validator rejected successfully");
        ResponseProtocol.sendSuccess(request, response, this, "Validator rejected successfully",
            Map.of("validatorId", validatorId),
            HttpServletResponse.SC_CREATED);
      }

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error accepting validator", e.getMessage(),
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
}