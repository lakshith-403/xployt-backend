package com.xployt.service.client;

import com.xployt.dao.client.HackerFilterDAO;
import com.xployt.dao.common.BlastPointsDAO;
import com.xployt.dao.common.InvitationDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Hacker;
import com.xployt.model.Invitation;

import java.sql.SQLException;
import java.util.*;

public class HackerFilterService {
  private final HackerFilterDAO hackerFilterDAO;
  private final InvitationDAO invitationDAO;
  private static final int MAX_SELECTED_HACKERS = 4;
  private static final double SIMILARITY_THRESHOLD = 0.1;
  private List<String> filteredHackers = new ArrayList<>();
  private final Set<String> redundantWords = new HashSet<>(Arrays.asList("Security", "Modules", "Processing", "Transfer",
      "Storage", "Gateway", "Compliance", "Data", "User", "Testing", "Management", "System", "Integration"));

  public HackerFilterService() {
    this.hackerFilterDAO = new HackerFilterDAO();
    this.invitationDAO = new InvitationDAO();
  }

  public GenericResponse filterHackers(String projectId) throws SQLException {
    System.out.println("HackerFilterService: Fetching hackers for project " + projectId);

    List<String> hackerIds = hackerFilterDAO.getAllHackerIds();

    filteredHackers = filterInvitedHackers(hackerIds, projectId);

    Set<String> projectSkills = prepareSkillList(hackerFilterDAO.getProjectSkills(projectId));

    List<Hacker> matchedHackers = filterHackersBySkills(filteredHackers, projectSkills);

    List<Hacker> selectedHackers = selectHackersWithSpreadBlastPoints(matchedHackers);

    if (!selectedHackers.isEmpty()) {
      return new GenericResponse(selectedHackers, true, null, null);
    }
    return new GenericResponse(null, false, "Failed to load hackers", null);
  }

  private List<String> filterInvitedHackers(List<String> hackerIds, String projectId) {
    System.out.println("Filtering invited hackers for project: " + projectId);
    List<String> filteredHackers = new ArrayList<>(hackerIds);
    try {
      List<Invitation> projectInvitations = invitationDAO.getProjectInvitations(projectId, false);

      for (Invitation invitation : projectInvitations) {
        if (invitation.getHackerId() != 0) {
          filteredHackers.remove(String.valueOf(invitation.getHackerId()));
        } else {
          System.out.println("Warning: Invitation with null HackerId found.");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error filtering invited hackers: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Unexpected error: " + e.getMessage());
    }
    return filteredHackers;
  }

  private List<Hacker> filterHackersBySkills(List<String> hackerIds, Set<String> projectSkills) throws SQLException {
    List<Hacker> matchedHackers = new ArrayList<>();
    for (String hackerId : hackerIds) {
      Set<String> hackerSkills = prepareSkillList(hackerFilterDAO.getHackerSkills(hackerId));

      double similarity = jacquardSimilarity(hackerSkills, projectSkills);

      if (similarity >= SIMILARITY_THRESHOLD) {
        Hacker hacker = hackerFilterDAO.getHackerById(hackerId);
        matchedHackers.add(hacker);
      }
    }
    return matchedHackers;
  }

  private List<Hacker> selectHackersWithSpreadBlastPoints(List<Hacker> hackers) throws SQLException {
    if (hackers.size() == MAX_SELECTED_HACKERS) {
      System.out.println("Returned hackers: " + hackers.size());
      return new ArrayList<>(hackers);
    }

    List<Hacker> selectedHackers = new ArrayList<>(hackers);
    ArrayList<Hacker> filteredHackersProfiles = new ArrayList<>();
    if (hackers.size() < MAX_SELECTED_HACKERS) {
      for (Hacker hacker : hackers) {
        filteredHackers.remove(hacker.getUserId());
      }
      for (String hackerId : filteredHackers) {
        Hacker hacker = hackerFilterDAO.getHackerById(hackerId);
        filteredHackersProfiles.add(hacker);
      }

      hackers.sort(Comparator.comparingInt(Hacker::getPoints).reversed());
      int step = Math.max(1, hackers.size() / MAX_SELECTED_HACKERS);
      for (int i = 0; i < hackers.size() && selectedHackers.size() < MAX_SELECTED_HACKERS; i += step) {
        selectedHackers.add(filteredHackersProfiles.get(i));
      }
      return selectedHackers;
    }


    hackers.sort(Comparator.comparingInt(Hacker::getPoints).reversed());
    int step = Math.max(1, hackers.size() / MAX_SELECTED_HACKERS);
    for (int i = 0; i < hackers.size() && selectedHackers.size() < MAX_SELECTED_HACKERS; i += step) {
      selectedHackers.add(hackers.get(i));
    }
    return selectedHackers;

  }

  //utils
  private double jacquardSimilarity(Set<String> set1, Set<String> set2) {
    Set<String> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);
    Set<String> union = new HashSet<>(set1);
    union.addAll(set2);
    return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
  }

  public Set<String> prepareSkillList(Set<String> skills) {
    Set<String> skillList = new HashSet<>();
    for (String skill : skills) {
      String[] words = skill.split("\\s+");
      Collections.addAll(skillList, words);
    }
    return removeRedundantWordsFromList(new ArrayList<>(skillList), redundantWords);
  }

  public Set<String> removeRedundantWordsFromList(List<String> list, Set<String> wordsToRemove) {
    Set<String> filteredList = new HashSet<>();
    for (String item : list) {
      boolean containsWord = false;
      for (String word : wordsToRemove) {
        if (item.contains(word)) {
          containsWord = true;
          break;
        }
      }
      if (!containsWord) {
        filteredList.add(item);
      }
    }
    return filteredList;
  }
}