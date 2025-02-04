package com.xployt.service.client;

import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.logging.Logger;
import java.sql.SQLException;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Map;

import com.xployt.dao.client.ProjectDAO;
import com.xployt.util.CustomLogger;
import com.xployt.model.Project;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseConfig;

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  /**
   * Method to create a project from JSON data
   * Initial project data from client request is entered into the database
   * A project lead is assigned to the project and added to the DB record
   * Project Lead is selected using a scoring algorithm
   * 
   * @return projectId
   */
  public int createProject(HttpServletRequest request, HttpServletResponse response) throws IOException {

    logger.info("Entering createProject method");
    // Read the JSON body from the request
    StringBuilder jsonBody = new StringBuilder();
    String line;
    try (BufferedReader reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        jsonBody.append(line);
      }
    } catch (IOException e) {
      logger.severe("IOException in createProject: " + e.getMessage());
      ResponseProtocol.sendError(request, response, "Error reading JSON",
          Map.of("error", "Error reading JSON"),
          HttpServletResponse.SC_BAD_REQUEST);
      return -1;
    }

    // Log the raw JSON data
    logger.info("Received JSON: " + jsonBody.toString());

    // Use ObjectMapper to parse the JSON into a Project object
    ObjectMapper objectMapper = new ObjectMapper();
    Project projectRequest;
    try {
      projectRequest = objectMapper.readValue(jsonBody.toString(), Project.class);
    } catch (IOException e) {
      logger.severe("IOException in createProject: " + e.getMessage());
      ResponseProtocol.sendError(request, response, "Error reading JSON",
          Map.of("error", "Error reading JSON"),
          HttpServletResponse.SC_BAD_REQUEST);
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
      ResponseProtocol.sendError(request, response, "Missing required parameters",
          Map.of("error", "Missing required parameters"),
          HttpServletResponse.SC_BAD_REQUEST);
      return -1;
    }

    // Proceed with project creation
    ProjectDAO projectDAO = new ProjectDAO();
    int projectId = -1;
    Connection conn = null;
    logger.info("ProjectService: Inside createProject");

    try {
      conn = DatabaseConfig.getConnection();
    } catch (Exception e) {
      e.printStackTrace();
      ResponseProtocol.sendError(request, response, "Error getting connection",
          Map.of("error", e.getMessage()),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return -1;
    }
    try {
      conn.setAutoCommit(false);
      projectId = projectDAO.createProject(Integer.parseInt(clientId), title, description, startDate, endDate, url,
          technicalStack,
          conn);
      int leadId = projectDAO.assignProjectLead(projectId, conn);
      conn.commit();
      logger.info("Project created successfully.");
      ResponseProtocol.sendSuccess(request, response, "Project created successfully",
          Map.of("projectId", projectId, "leadId", leadId),
          HttpServletResponse.SC_OK);
    } catch (Exception e) {
      try {
        conn.rollback();
      } catch (SQLException e1) {
        logger.severe("Error rolling back transaction: " + e1.getMessage());
      }
      logger.severe("Error creating project: " + e.getMessage());
      ResponseProtocol.sendError(request, response, "Error creating project",
          Map.of("error", e.getMessage()),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return -1;
    }
    return projectId;
  }
}
