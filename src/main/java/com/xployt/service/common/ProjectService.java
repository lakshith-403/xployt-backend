package com.xployt.service.common;

import com.xployt.dao.common.ProjectDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Project;
import com.xployt.model.ProjectBrief;
import com.xployt.util.CustomLogger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import com.xployt.util.JsonUtil;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ProjectService {
    private final ProjectDAO projectDAO;
    private static final List<String> STATUS_FILTER = Arrays.asList("closed");
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public void fetchProjects(String userId, HttpServletResponse response) throws IOException {
        logger.info("Fetching projects for userId: " + userId);
        List<ProjectBrief> allProjects = projectDAO.getAllProjects(userId);

        List<ProjectBrief> filteredProjects = new ArrayList<>();
        List<ProjectBrief> remainingProjects = new ArrayList<>();

        for (ProjectBrief project : allProjects) {
            if (!STATUS_FILTER.contains(project.getState())) {
                filteredProjects.add(project);
            } else {
                remainingProjects.add(project);
            }
        }

        logger.info("Filtered projects: " + filteredProjects.size());
        List<List<ProjectBrief>> result = new ArrayList<>();
        result.add(filteredProjects);
        result.add(remainingProjects);

        try {
            response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, true, null, null)));
        } catch (IOException e) {
            logger.severe("Error writing response: " + e.getMessage());
            response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, false, null, null)));
        }
    }

    public GenericResponse getProjectById(String projectId) throws SQLException {
        logger.info("Fetching project of projectId: " + projectId);
        Project project = projectDAO.getProjectById(projectId);
        if(project != null) {
            return new GenericResponse(project, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to fetch project", null);
    }

    public GenericResponse getProjectSeverityLevels(int projectId) {
        logger.info("Fetching project severity levels for projectId: " + projectId);
        ArrayList<String[]> severityLevels = projectDAO.getProjectSeverityLevels(projectId);
        if (severityLevels != null) {
            return new GenericResponse(severityLevels, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to fetch severity levels", null);
    }
}