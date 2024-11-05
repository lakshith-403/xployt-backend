package com.xployt.service;

import com.xployt.dao.ProjectDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Project;
import com.xployt.util.CustomLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ProjectService {
    private ProjectDAO projectDAO;
    private static final List<String> STATUS_FILTER = Arrays.asList("closed"); // Example statuses to filter
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public GenericResponse fetchProjects(String userId) {
        logger.info("Fetching projects for userId: " + userId);
        List<Project> allProjects = projectDAO.getAllProjects(userId);

        List<Project> filteredProjects = new ArrayList<>();
        List<Project> remainingProjects = new ArrayList<>();

        for (Project project : allProjects) {
            if (!STATUS_FILTER.contains(project.getStatus())) {
                filteredProjects.add(project);
            } else {
                remainingProjects.add(project);
            }
        }

        logger.info("Filtered projects: " + filteredProjects.size());
        List<List<Project>> result = new ArrayList<>();
        result.add(filteredProjects);
        result.add(remainingProjects);

        return new GenericResponse(result, true, null, null);
    }
}