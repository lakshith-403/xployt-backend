package com.xployt.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.xployt.dao.common.ProjectTeamAssignmentDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.PublicUser;
// import com.xployt.service.lead.ProjectService;
import com.xployt.util.CustomLogger;

public class ProjectTeamAssignmentService {
    private final ProjectTeamAssignmentDAO teamAssignmentDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectTeamAssignmentService() {
        this.teamAssignmentDAO = new ProjectTeamAssignmentDAO();
    }

    // public GenericResponse assignValidators(String projectId, int validatorCount) {
    //     try {
    //         List<String> assignedValidators = teamAssignmentDAO.assignValidatorsToProject(projectId, validatorCount);
            
    //         if (assignedValidators.isEmpty()) {
    //             return new GenericResponse(
    //                 null,
    //                 false,
    //                 "No validators available",
    //                 "Could not find any validators to assign"
    //             );
    //         }
            
    //         // Create a discussion with all validators and the project lead
    //         if (!assignedValidators.isEmpty()) {
    //             logger.info("Creating validator discussion for project: " + projectId);
    //             ProjectService projectService = new ProjectService();
    //             projectService.createValidatorDiscussion(projectId);
    //         }
    
    //         return new GenericResponse(
    //             assignedValidators,
    //             true,
    //             "Successfully assigned " + assignedValidators.size() + " validators to project",
    //             null
    //         );
            
    //     } catch (Exception e) {
    //         logger.severe("Error in assignValidators: " + e.getMessage());
    //         return new GenericResponse(
    //             null,
    //             false,
    //             "Failed to assign validators",
    //             e.getMessage()
    //         );
    //     }
    // }

    public GenericResponse getAssignedUser(String validatorId, String projectId, String requiredRole){
        List<PublicUser> users = new ArrayList<>();
        if(Objects.equals(requiredRole, "hacker")){
            users = teamAssignmentDAO.getAssignedHacker(validatorId, projectId);
        } else if(Objects.equals(requiredRole, "validator")){
            users = teamAssignmentDAO.getAssignedValidator(validatorId, projectId);
        } else{
            return new GenericResponse(null, false, "Invalid role", "Invalid role");
        }
        return new GenericResponse(users, true, "Successfully fetched assigned users", null);
    }
}