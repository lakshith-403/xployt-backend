package com.xployt.service.common;

import com.xployt.dao.common.UserProjectsDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectBrief;
import com.xployt.util.CustomLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UserProjectsService {
    public GenericResponse fetchUserProjects(String userId, String userStatus) throws IOException {
        final List<String> INACTIVE_FILTER = Arrays.asList("Completed", "Rejected");
        final List<String> REQUESTS_FILTER = Arrays.asList("Pending", "Unconfigured");
        final Logger logger = CustomLogger.getLogger();

        logger.info("UserProjectsService: Inside fetchProjects");

        UserProjectsDAO userProjectsDAO = new UserProjectsDAO();
        List<ProjectBrief> allProjects = userProjectsDAO.getAllProjects(userId, userStatus);

        List<ProjectBrief> requestedProjects = new ArrayList<>();
        List<ProjectBrief> inactiveProjects = new ArrayList<>();
        List<ProjectBrief> activeProjects = new ArrayList<>();

        for (ProjectBrief project : allProjects) {
            if (INACTIVE_FILTER.contains(project.getState())) {
                inactiveProjects.add(project);
            } else if (REQUESTS_FILTER.contains(project.getState())) {
                requestedProjects.add(project);
            } else {
                activeProjects.add(project);
            }
        }

        logger.info("UserProjectsService: Filtered projects: " + requestedProjects.size());
        Map<String, List<ProjectBrief>> result = Map.of(
                "activeProjects", activeProjects,
                "requestedProjects", requestedProjects,
                "inactiveProjects", inactiveProjects
        );

        return new GenericResponse(result, true, null, null);
    }
}