package com.xployt.service.lead;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xployt.dao.lead.ProjectConfigInfoDAO;
import com.xployt.util.JsonUtil;
import com.xployt.model.GenericResponse;
import com.xployt.util.ResponseUtil;
import com.xployt.model.ProjectConfigInfo;
import com.xployt.model.ProjectConfig;
import com.xployt.dao.lead.ProjectConfigDAO;

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

    ProjectConfigDAO projectConfigDAO = new ProjectConfigDAO();
    projectConfigDAO.updateProjectConfig(projectConfig);
  }
}
