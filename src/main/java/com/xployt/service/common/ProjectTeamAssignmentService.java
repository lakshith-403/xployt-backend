package com.xployt.service.common;

import com.xployt.dao.common.ProjectTeamAssignmentDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.PublicUser;
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

    public GenericResponse getAssignedValidator(String hackerId, String projectId){
        logger.info("Getting assigned validator for project");
        PublicUser validator = teamAssignmentDAO.getAssignedValidator(hackerId, projectId);
        if (validator != null) {
            return new GenericResponse(validator, true, null, null);
        }
        return new GenericResponse(null, false, "No validator assigned", "No validator assigned to project");

    }
    public GenericResponse getAssignedHacker(String validatorId, String projectId){
        logger.info("Getting assigned validator for project");
        PublicUser hacker = teamAssignmentDAO.getAssignedHacker(validatorId, projectId);
        if (hacker != null) {
            return new GenericResponse(hacker, true, null, null);
        }
        return new GenericResponse(null, false, "No validator assigned", "No validator assigned to project");

    }
}