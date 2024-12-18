package com.xployt.service.client;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Logger;
import java.sql.SQLException;
import java.io.BufferedReader;

import com.xployt.dao.client.ProjectDAO;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.Project;
import com.xployt.model.GenericResponse;
import com.xployt.util.JsonUtil;

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  // Method to create a project from JSON data
  public int createProject(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Log entry into the method
    logger.info("Entering createProject method");

    // Read the JSON body from the request
    StringBuilder jsonBody = new StringBuilder();
    String line;
    try (BufferedReader reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        jsonBody.append(line);
      }
    }

    // Log the raw JSON data
    logger.info("Received JSON: " + jsonBody.toString());

    // Use ObjectMapper to parse the JSON into a Project object
    ObjectMapper objectMapper = new ObjectMapper();
    Project projectRequest;
    try {
      projectRequest = objectMapper.readValue(jsonBody.toString(), Project.class);
    } catch (IOException e) {
      logger.severe("Error parsing JSON: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(null, false, e.getMessage(), null)));
      return -1;
    }

    // Log the parsed project details
    logger.info("Parsed Project: " + projectRequest);

    // Extract project details
    String clientId = projectRequest.getClientId();
    String title = projectRequest.getTitle();
    String description = projectRequest.getDescription();
    String startDate = projectRequest.getStartDate();
    String endDate = projectRequest.getEndDate();
    String url = projectRequest.getUrl();
    String technicalStack = projectRequest.getTechnicalStack();

    // Log the extracted project details
    logger.info("Project Details - Client ID: " + clientId + ", Title: " + title);

    // Validation check for required parameters
    if (clientId == null || title == null || description == null || startDate == null
        || endDate == null) {
      logger.severe("Missing required parameters in JSON payload.");
      response.getWriter()
          .write(JsonUtil.toJson(new GenericResponse(null, false, "Missing required parameters", null)));
      return -1;
    }

    // Proceed with project creation
    ProjectDAO projectDAO = new ProjectDAO();
    int projectId = -1;
    Connection conn = null;
    logger.info("ProjectService: Inside createProject");

    try {
      ServletContext servletContext = ContextManager.getContext("DBConnection");
      if (servletContext == null) {
        throw new NullPointerException("ServletContext is null");
      }
      conn = (Connection) servletContext.getAttribute("DBConnection");
      if (conn == null) {
        throw new NullPointerException("DBConnection is null");
      }
      conn.setAutoCommit(false);
      projectId = projectDAO.createProject(Integer.parseInt(clientId), title, description, startDate, endDate, url,
          technicalStack,
          conn);
      projectDAO.assignProjectLead(projectId, conn);
      conn.commit();
      logger.info("Project created successfully.");
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(null, true, null, null)));
    } catch (Exception e) {
      try {
        conn.rollback();
      } catch (SQLException e1) {
        logger.severe("Error rolling back transaction: " + e1.getMessage());
      }
      logger.severe("Error creating project: " + e.getMessage());
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(null, false, e.getMessage(), null)));
      return -1;
    }
    return projectId;
  }
}
