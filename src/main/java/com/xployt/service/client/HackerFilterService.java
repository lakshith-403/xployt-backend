package com.xployt.service.client;

import com.xployt.dao.client.HackerFilterDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Hacker;
import com.xployt.util.CustomLogger;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class HackerFilterService {
    private final HackerFilterDAO hackerFilterDAO;
    private static final Logger logger = CustomLogger.getLogger();
    private static final int MAX_SELECTED_HACKERS = 4;
    private static final double SIMILARITY_THRESHOLD = 0.1;

    public HackerFilterService() {
        this.hackerFilterDAO = new HackerFilterDAO();
    }

    public GenericResponse filterHackers(String projectId) throws SQLException {
        logger.info("HackerFilterService: Fetching hackers for project " + projectId);
        List<String> hackerIds = hackerFilterDAO.getAllHackerIds();
        Set<String> projectSkills = hackerFilterDAO.getProjectSkills(projectId);
        List<Hacker> matchedHackers = filterHackersBySkills(hackerIds, projectSkills);
        List<Hacker> selectedHackers = selectHackersWithSpreadBlastPoints(matchedHackers);

        if (!selectedHackers.isEmpty()) {
            return new GenericResponse(selectedHackers, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to load hackers", null);
    }

    private List<Hacker> filterHackersBySkills(List<String> hackerIds, Set<String> projectSkills) throws SQLException {
        List<Hacker> matchedHackers = new ArrayList<>();
        for (String hackerId : hackerIds) {
            Set<String> hackerSkills = hackerFilterDAO.getHackerSkills(hackerId);
            double similarity = jacquardSimilarity(hackerSkills, projectSkills);
            if (similarity >= SIMILARITY_THRESHOLD) {
                Hacker hacker = hackerFilterDAO.getHackerById(hackerId);
                matchedHackers.add(hacker);
            }
        }
        return matchedHackers;
    }

    private double jacquardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private List<Hacker> selectHackersWithSpreadBlastPoints(List<Hacker> hackers) {
        if (hackers.size() <= MAX_SELECTED_HACKERS) {
            return hackers;
        }

        List<Hacker> selectedHackers = new ArrayList<>();
        int step = hackers.size() / MAX_SELECTED_HACKERS;

        for (int i = 0; i < MAX_SELECTED_HACKERS; i++) {
            selectedHackers.add(hackers.get(i * step));
        }

        Random random = new Random();
        while (selectedHackers.size() < MAX_SELECTED_HACKERS) {
            Hacker randomHacker = hackers.get(random.nextInt(hackers.size()));
            if (!selectedHackers.contains(randomHacker)) {
                selectedHackers.add(randomHacker);
            }
        }

        return selectedHackers;
    }
}