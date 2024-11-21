package com.xployt.service.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xployt.dao.client.ProjectDAO;
import com.xployt.util.CustomLogger;
import com.fasterxml.jackson.databind.ObjectMapper; // Add Jackson dependency for JSON parsing
import java.io.IOException;
import java.util.logging.Logger;
import com.xployt.model.CreateProject;

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  // Method to create a project from JSON data
  public void createProject(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read the JSON body from the request
    StringBuilder jsonBody = new StringBuilder();
    String line;
    try (var reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        jsonBody.append(line);
      }
    }

    // Log the raw JSON data for debugging
    logger.info("Received JSON: " + jsonBody.toString());

    // Use ObjectMapper to parse the JSON into a Project object
    ObjectMapper objectMapper = new ObjectMapper();
    CreateProject projectRequest;
    try {
      projectRequest = objectMapper.readValue(jsonBody.toString(), CreateProject.class);
    } catch (IOException e) {
      logger.severe("Error parsing JSON: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
      return;
    }

    // Now you have the data in the projectRequest object
    String clientId = projectRequest.getClientId();
    String projectTitle = projectRequest.getProjectTitle();
    String projectDescription = projectRequest.getProjectDescription();
    String startDate = projectRequest.getStartDate();
    String endDate = projectRequest.getEndDate();
    String url = projectRequest.getUrl();
    String technicalStack = projectRequest.getTechnicalStack();

    // Validation check for required parameters
    if (clientId == null || projectTitle == null || projectDescription == null || startDate == null
        || endDate == null) {
      logger.severe("Missing required parameters in JSON payload.");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
      return;
    }

    // Proceed with project creation
    ProjectDAO projectDAO = new ProjectDAO();
    logger.info("ProjectService: Inside createProject");

    // Logging the parsed data for debugging
    logger.info("clientId: " + clientId);
    logger.info("projectTitle: " + projectTitle);
    logger.info("projectDescription: " + projectDescription);
    logger.info("startDate: " + startDate);
    logger.info("endDate: " + endDate);
    logger.info("url: " + url);
    logger.info("technicalStack: " + technicalStack);

    try {
      projectDAO.createProject(clientId, projectTitle, projectDescription, startDate, endDate, url, technicalStack);
      logger.info("Project created successfully.");
    } catch (Exception e) {
      logger.severe("Error creating project: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating project");
    }
  }
}
