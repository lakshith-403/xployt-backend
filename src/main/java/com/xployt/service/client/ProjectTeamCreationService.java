package com.xployt.service.client;

import com.xployt.dao.client.ProjectTeamCreationDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectTeamRequest;
import com.xployt.util.CustomLogger;

import java.util.logging.Logger;

public class ProjectTeamCreationService {
    private final ProjectTeamCreationDAO projectTeamCreationDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectTeamCreationService() {
        this.projectTeamCreationDAO = new ProjectTeamCreationDAO();
    }

    public GenericResponse createProjectTeam(ProjectTeamRequest request) {
        try {
            // Create project with lead
            projectTeamCreationDAO.createProject(
                request.getProjectId(),
                request.getClientId(),
                request.getProjectLeadId()
            );

            // Assign validators
            projectTeamCreationDAO.assignValidators(
                request.getProjectId(),
                request.getNumberOfValidators()
            );

            return new GenericResponse(
                null,
                true,
                "Project team created successfully",
                null
            );

        } catch (Exception e) {
            logger.severe("Error in createProjectTeam: " + e.getMessage());
            return new GenericResponse(
                null,
                false,
                "Failed to create project team",
                e.getMessage()
            );
        }
    }
} 