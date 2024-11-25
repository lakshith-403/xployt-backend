package com.xployt.service.common;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectTeam;
import com.xployt.util.CustomLogger;

public class ProjectTeamService {
    private final ProjectTeamDAO projectTeamDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectTeamService() {
        this.projectTeamDAO = new ProjectTeamDAO();
    }

    public GenericResponse fetchProjectTeam(String projectId) throws SQLException {
        logger.log(Level.INFO, "Fetching project team for projectId: {0}", projectId);
        ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
        if(projectTeam != null) {
            return new GenericResponse(projectTeam, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to fetch project team", null);
    }
}
