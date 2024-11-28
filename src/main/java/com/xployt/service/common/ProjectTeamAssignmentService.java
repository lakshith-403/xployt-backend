package com.xployt.service.common;

import com.xployt.dao.common.ProjectTeamAssignmentDAO;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;
import java.util.List;
import java.util.logging.Logger;

public class ProjectTeamAssignmentService {
    private final ProjectTeamAssignmentDAO teamAssignmentDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectTeamAssignmentService() {
        this.teamAssignmentDAO = new ProjectTeamAssignmentDAO();
    }

    public GenericResponse assignValidators(String projectId, int validatorCount) {
        try {
            List<String> assignedValidators = teamAssignmentDAO.assignValidatorsToProject(projectId, validatorCount);
            
            if (assignedValidators.isEmpty()) {
                return new GenericResponse(
                    null, 
                    false, 
                    "No validators available", 
                    "Could not find any validators to assign"
                );
            }
    
            return new GenericResponse(
                assignedValidators, 
                true, 
                "Successfully assigned " + assignedValidators.size() + " validators to project",
                null
            );
            
        } catch (Exception e) {
            logger.severe("Error in assignValidators: " + e.getMessage());
            return new GenericResponse(
                null, 
                false, 
                "Failed to assign validators", 
                e.getMessage()
            );
        }
    }
}