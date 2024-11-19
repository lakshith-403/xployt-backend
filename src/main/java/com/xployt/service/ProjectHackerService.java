package com.xployt.service;

import com.xployt.dao.projectHackerDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectHacker;
import com.xployt.util.CustomLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class ProjectHackerService {
    private projectHackerDAO projectHackerDAO;
    private static final List<String> STATUS_FILTER = Arrays.asList("Invited");
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectHackerService() {this.projectHackerDAO = new projectHackerDAO();}

    public GenericResponse fetchProjectHackers() {
        logger.info("ProjectHackerService: Fetching projectHackers");

        List<ProjectHacker> allProjectHackers = projectHackerDAO.getProjectHackers();

        List<ProjectHacker> filteredProjectHackers = new ArrayList<>();
        List<ProjectHacker> remainingProjectHackers = new ArrayList<>();

        for(ProjectHacker projectHacker : allProjectHackers) {
            if(STATUS_FILTER.contains(projectHacker.getStatus())) {
                filteredProjectHackers.add(projectHacker);
            }else{
                remainingProjectHackers.add(projectHacker);
            }
        }

        logger.info("FilteredProjectHackers: " + filteredProjectHackers.size());
        List<List<ProjectHacker>> result = new ArrayList<>();
        result.add(filteredProjectHackers);
        result.add(remainingProjectHackers);

        return new GenericResponse(result, true, null, null);

    }
}
