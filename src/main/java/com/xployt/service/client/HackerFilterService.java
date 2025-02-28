package com.xployt.service.client;

import com.xployt.dao.client.HackerFilterDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.PublicUser;
import com.xployt.util.CustomLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class HackerFilterService {
    private final HackerFilterDAO hackerFilterDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public HackerFilterService() {this.hackerFilterDAO = new HackerFilterDAO();}

    public GenericResponse filterHackers(String projectId) throws SQLException {
        logger.info("HackerFilterService: Fetching hackers for project " + projectId);
        List<PublicUser> hackers = hackerFilterDAO.filterHackersForProject(projectId);
        if(!hackers.isEmpty()) {
            return new GenericResponse(hackers, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to load hackers", null);
    }


}
