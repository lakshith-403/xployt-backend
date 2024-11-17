package com.xployt.service.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xployt.dao.client.ProjectDAO;
import java.io.IOException;
import com.xployt.util.CustomLogger;
import java.util.logging.Logger;

class Options {
  final String clientId;
  final String projectTitle;
  final String projectDescription;
  final String startDate;
  final String endDate;
  final String url;
  final String technicalStack;

  Options(String clientId, String projectTitle, String projectDescription, String startDate, String endDate, String url,
      String technicalStack) {
    this.clientId = clientId;
    this.projectTitle = projectTitle;
    this.projectDescription = projectDescription;
    this.startDate = startDate;
    this.endDate = endDate;
    this.url = url;
    this.technicalStack = technicalStack;

  }
}

public class ProjectService {

  private static final Logger logger = CustomLogger.getLogger();

  public void createProject(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String clientId = request.getParameter("clientId");
    String projectTitle = request.getParameter("projectTitle");
    String projectDescription = request.getParameter("projectDescription");
    String startDate = combineDate(request.getParameter("startDay"), request.getParameter("startMonth"),
        request.getParameter("startYear"));
    String endDate = combineDate(request.getParameter("endDay"), request.getParameter("endMonth"),
        request.getParameter("endYear"));
    String url = request.getParameter("url");
    String technicalStack = request.getParameter("technicalStack");

    ProjectDAO projectDAO = new ProjectDAO();
    logger.info("ProjectService: Inside createProject");
    logger.info("clientId: " + clientId);
    logger.info("projectTitle: " + projectTitle);
    logger.info("projectDescription: " + projectDescription);
    logger.info("startDate: " + startDate);
    logger.info("endDate: " + endDate);
    logger.info("url: " + url);
    logger.info("technicalStack: " + technicalStack);
    projectDAO.createProject(clientId, projectTitle, projectDescription, startDate, endDate, url, technicalStack);
  }

  private String combineDate(String day, String month, String year) {
    return year + "-" + month + "-" + day;
  }
}
