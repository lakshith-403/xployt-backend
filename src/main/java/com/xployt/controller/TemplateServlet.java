package com.xployt.controller;

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

@WebServlet("/api/admin/manageValidator")
public class TemplateServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("\n------------ ManageValidatorServlet | doPut ------------");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ TemplateServlet | doPost ------------");

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
          "Password123",
          requestBody.get("name") });

      sqlParams.add(new Object[] { requestBody.get("email") });

      List<Map<String, Object>> rs = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (rs.size() > 0) {
        System.out.println("After rs.next()");
        validatorId = (int) rs.get(0).get("userId");
        System.out.println("Validator ID: " + validatorId);
      }

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Validator created successfully");

      ResponseProtocol.sendSuccess(request, response, this, "Validator created successfully",
          Map.of("validatorId", validatorId),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

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