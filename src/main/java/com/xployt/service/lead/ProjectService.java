package com.xployt.service.lead;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;
import com.xployt.dao.lead.ProjectDAO;
import com.xployt.util.JsonUtil;
import com.xployt.model.GenericResponse;
import com.xployt.util.ResponseUtil;

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  public void getProjectInfo(HttpServletRequest request, HttpServletResponse response) {

    logger.info("ProjectService getProjectInfo method called");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      ResponseUtil.writeResponse(response,
          JsonUtil.toJson(new GenericResponse(null, false, "User ID not provided hehe hoo", null)));
      return;
    }
    String projectId = pathInfo.substring(1);
    ProjectDAO projectDAO = new ProjectDAO();
    projectDAO.getProjectInfo(projectId);
  }
}
