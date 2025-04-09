package com.xployt.service.lead;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xployt.dao.common.DiscussionDAO;
import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.dao.lead.ProjectConfigDAO;
import com.xployt.dao.lead.ProjectConfigInfoDAO;
import com.xployt.dao.lead.ProjectDAO;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectConfig;
import com.xployt.model.ProjectConfigInfo;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.ResponseUtil;

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  public void getProjectConfigInfo(HttpServletRequest request, HttpServletResponse response) {

    logger.info("ProjectService getProjectInfo method called");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      ResponseUtil.writeResponse(response,
          JsonUtil.toJson(new GenericResponse(null, false, "User ID not provided hehe hoo", null)));
      return;
    }
    String projectId = pathInfo.substring(1);
    ProjectConfigInfoDAO projectConfigInfoDAO = new ProjectConfigInfoDAO();
    try {
      ProjectConfigInfo result = projectConfigInfoDAO.getProjectInfo(projectId);
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(result, true, null, null)));

    } catch (Exception e) {
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(null, false, null, null)));
    }
  }

  public void updateProjectConfigInfo(HttpServletRequest request, HttpServletResponse response) {
    logger.info("ProjectService updateProjectConfigInfo method called");
    StringBuilder jsonBody = new StringBuilder();
    String line;
    try (var reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        jsonBody.append(line);
      }
    } catch (IOException e) {
      logger.severe("Error reading request body: " + e.getMessage());
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(null, false, e.getMessage(), null)));
      return;
    }

    logger.info("Received JSON: " + jsonBody.toString());

    // Use ObjectMapper to parse the JSON into a Project object
    ObjectMapper objectMapper = new ObjectMapper();
    ProjectConfig projectConfig;
    try {
      projectConfig = objectMapper.readValue(jsonBody.toString(), ProjectConfig.class);
    } catch (IOException e) {
      logger.severe("Error parsing JSON: " + e.getMessage());
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(null, false, e.getMessage(), null)));
      return;
    }

    ProjectDAO projectDAO = new ProjectDAO();
    ProjectConfigDAO projectConfigDAO = new ProjectConfigDAO();
    try {
      projectConfigDAO.updateProjectConfig(projectConfig);
      projectDAO.updateProjectState(projectConfig.getProjectId(), "Active");
    } catch (SQLException e) {
      logger.severe("SQL Error in updateProjectConfigInfo: " + e.getMessage());
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(null, false, e.getMessage(), null)));
      return;
    }
  }

  public void createValidatorDiscussion(String projectId) {
    try {
      logger.info("Creating discussion with validators and lead for project: " + projectId);
      ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
      ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);

      // Create a list of participants with the project lead and all validators
      List<PublicUser> participants = new ArrayList<>();
      
      // Add project lead
      participants.add(new PublicUser(projectTeam.getProjectLead().getUserId(), 
                                     projectTeam.getProjectLead().getName(),
                                     projectTeam.getProjectLead().getEmail()));
      
      // Add all validators
      List<PublicUser> validators = projectTeam.getProjectValidators();
      if (validators != null && !validators.isEmpty()) {
        for (PublicUser validator : validators) {
          participants.add(new PublicUser(validator.getUserId(), 
                                         validator.getName(), 
                                         validator.getEmail()));
        }
        
        // Create and save the discussion
        DiscussionDAO discussionDAO = new DiscussionDAO();
        Discussion discussion = new Discussion(
            UUID.randomUUID().toString(), 
            "Validator Team Discussion", 
            participants, 
            new Date(),
            projectId, 
            new ArrayList<>()
        );
        discussionDAO.createDiscussion(discussion);
        logger.info("Successfully created validator discussion for project: " + projectId);
      } else {
        logger.warning("No validators found for project: " + projectId);
      }
    } catch (Exception e) {
      logger.severe("Error creating discussion with validators and lead: " + e.getMessage());
    }
  }

  public void acceptProject(String projectId, HttpServletResponse response, HttpServletRequest request)
      throws IOException {
    ProjectDAO projectDAO = new ProjectDAO();

    // create a discussion and add to the project
    try {
      ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
      ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);

      List<PublicUser> participants = new ArrayList<>();
      participants.add(new PublicUser(projectTeam.getClient().getUserId(), projectTeam.getClient().getName(),
          projectTeam.getClient().getEmail()));
      participants.add(new PublicUser(projectTeam.getProjectLead().getUserId(), projectTeam.getProjectLead().getName(),
          projectTeam.getProjectLead().getEmail()));

      DiscussionDAO discussionDAO = new DiscussionDAO();
      Discussion discussion = new Discussion(UUID.randomUUID().toString(), "Init Project", participants, new Date(),
          projectId, new ArrayList<>());
      discussionDAO.createDiscussion(discussion);
    } catch (Exception e) {
      logger.severe("Error creating discussion with client and lead: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error creating discussion with client and lead");
      return;
    }

    logger.info("ProjectService acceptProject method called for projectId: " + projectId);
    try {
      projectDAO.updateProjectState(projectId, "Unconfigured");

      ResponseProtocol.sendSuccess(request, response, "Project accepted", null,
          HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.severe("Error accepting project: " + e.getMessage());
      ResponseProtocol.sendError(request, response, "Error accepting project", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public void rejectProject(String projectId, HttpServletResponse response, HttpServletRequest request)
      throws IOException {
    ProjectDAO projectDAO = new ProjectDAO();

    logger.info("ProjectService rejectProject method called for projectId: " + projectId);
    try {
      projectDAO.updateProjectState(projectId, "Rejected");

      ResponseProtocol.sendSuccess(request, response, "Project rejected", null,
          HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.severe("Error rejecting project: " + e.getMessage());
      ResponseProtocol.sendError(request, response, "Error rejecting project", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
