package com.xployt.service.common;

import com.xployt.dao.ProjectDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectBrief;
import com.xployt.util.CustomLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import com.xployt.util.JsonUtil;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ProjectService {
    private ProjectDAO projectDAO;
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
            if (!STATUS_FILTER.contains(project.getStatus())) {
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
}