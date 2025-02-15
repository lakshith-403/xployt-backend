package com.xployt.dao.lead;

import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;
import com.xployt.util.CustomLogger;
import com.xployt.util.ContextManager;

public class ProjectValidatorsDAO {
  private static final Logger logger = CustomLogger.getLogger();

  // Weight constants for different factors
  private static final double SKILL_MATCH_WEIGHT = 0.5; // 50% importance
  private static final double PROJECT_COUNT_WEIGHT = 0.3; // 30% importance
  private static final double WORKLOAD_BALANCE_WEIGHT = 0.2; // 20% importance

  public List<String> assignValidatorsBasedOnSkills(int projectId, int validatorCount, Connection conn)
      throws Exception {

    List<String> assignedValidators = new ArrayList<>();
    if (conn == null) {
      conn = (Connection) ContextManager.getContext("DBConnection").getAttribute("DBConnection");
    }

    try {
      conn.setAutoCommit(false);

      // Step 1: Fetch Project Scopes
      List<Integer> Skills = fetchSkills(conn, projectId);
      System.out.println("Project skills: " + Skills);

      // Step 2: Calculate Validator Scores
      List<ValidatorScore> validatorScores = calculateValidatorScores(conn, Skills);

      // Step 3: Assign Top Validators
      assignTopValidators(conn, projectId, validatorCount, validatorScores, assignedValidators);

      conn.commit();
      return assignedValidators;

    } catch (Exception e) {
      conn.rollback();
      logger.severe("Error assigning validators: " + e.getMessage());
      throw e;
    }
  }

  private List<Integer> fetchSkills(Connection conn, int projectId) throws Exception {
    String sql = "SELECT DISTINCT s2s.skillId FROM ProjectScope ps " +
        "JOIN scopeToSkills s2s ON ps.scopeId = s2s.scopeId " +
        "WHERE ps.projectId = ?";
    List<Integer> Skills = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, projectId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        Skills.add(rs.getInt("skillId"));
      }
    }
    return Skills;
  }

  private List<ValidatorScore> calculateValidatorScores(Connection conn, List<Integer> Skills) throws Exception {
    Map<String, Double> skillMatchScores = getSkillMatchScores(conn, Skills);
    Map<String, Double> projectCountScores = getProjectCountScores(conn);
    Map<String, Double> workloadScores = getWorkloadScores(conn);

    // Combine the scores into a single list of ValidatorScore objects
    List<ValidatorScore> validatorScores = new ArrayList<>();
    for (String userId : skillMatchScores.keySet()) {
      double skillScore = skillMatchScores.getOrDefault(userId, 0.0);
      double projectScore = projectCountScores.getOrDefault(userId, 1.0); // Default 1.0 if not present
      double workloadScore = workloadScores.getOrDefault(userId, 1.0); // Default 1.0 if not present
      validatorScores.add(new ValidatorScore(userId, skillScore, projectScore, workloadScore));
    }

    return validatorScores;
  }

  private Map<String, Double> getSkillMatchScores(Connection conn, List<Integer> Skills) throws Exception {
    String sql = "SELECT v.userId, (COUNT(DISTINCT CASE WHEN vs.skillId IN (?) THEN vs.skillId END) / ?) * ? AS skill_match_score "
        +
        "FROM Users v " +
        "LEFT JOIN ValidatorSkillSet vs ON v.userId = vs.validatorId " +
        "WHERE v.role = 'Validator' " +
        "GROUP BY v.userId";

    Map<String, Double> skillScores = new HashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, Skills.toString().replace("[", "").replace("]", ""));
      stmt.setInt(2, Skills.size());
      stmt.setDouble(3, SKILL_MATCH_WEIGHT);

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        skillScores.put(rs.getString("userId"), rs.getDouble("skill_match_score"));
      }
    }
    return skillScores;
  }

  private Map<String, Double> getProjectCountScores(Connection conn) throws Exception {
    String sql = "SELECT v.userId, " +
        "(1 - (COUNT(DISTINCT pv.projectId) / " +
        "(SELECT MAX(project_count) + 1 FROM (SELECT COUNT(*) as project_count FROM ProjectValidators GROUP BY validatorId) counts))) * ? AS project_count_score "
        +
        "FROM Users v " +
        "LEFT JOIN ProjectValidators pv ON v.userId = pv.validatorId " +
        "WHERE v.role = 'Validator' " +
        "GROUP BY v.userId";

    Map<String, Double> projectScores = new HashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, PROJECT_COUNT_WEIGHT);

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        projectScores.put(rs.getString("userId"), rs.getDouble("project_count_score"));
      }
    }
    return projectScores;
  }

  private Map<String, Double> getWorkloadScores(Connection conn) throws Exception {
    String sql = "SELECT v.userId, " +
        "(1 - (COUNT(DISTINCT CASE WHEN p.state = 'Active' THEN pv.projectId END) / " +
        "(SELECT MAX(active_count) + 1 FROM (SELECT COUNT(*) as active_count FROM ProjectValidators " +
        "JOIN Projects ON ProjectValidators.projectId = Projects.projectId " +
        "WHERE Projects.state = 'Active' GROUP BY validatorId) counts))) * ? AS workload_score " +
        "FROM Users v " +
        "LEFT JOIN ProjectValidators pv ON v.userId = pv.validatorId " +
        "LEFT JOIN Projects p ON pv.projectId = p.projectId " +
        "WHERE v.role = 'Validator' " +
        "GROUP BY v.userId";

    Map<String, Double> workloadScores = new HashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, WORKLOAD_BALANCE_WEIGHT);

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        workloadScores.put(rs.getString("userId"), rs.getDouble("workload_score"));
      }
    }
    return workloadScores;
  }

  private void assignTopValidators(Connection conn, int projectId, int validatorCount,
      List<ValidatorScore> validatorScores, List<String> assignedValidators) throws Exception {

    validatorScores.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

    String insertSQL = "INSERT INTO ProjectValidators (projectId, validatorId) VALUES (?, ?)";
    for (int i = 0; i < Math.min(validatorCount, validatorScores.size()); i++) {
      String validatorId = validatorScores.get(i).getUserId();
      assignedValidators.add(validatorId);

      try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
        insertStmt.setInt(1, projectId);
        insertStmt.setString(2, validatorId);
        insertStmt.executeUpdate();
      }

      logger.info(String.format("Assigned validator %s to project %s with score %f",
          validatorId, projectId, validatorScores.get(i).getTotalScore()));
    }
  }

  private static class ValidatorScore {
    private final String userId;
    private final double skillMatchScore;
    private final double projectCountScore;
    private final double workloadScore;

    public ValidatorScore(String userId, double skillMatchScore, double projectCountScore, double workloadScore) {
      this.userId = userId;
      this.skillMatchScore = skillMatchScore;
      this.projectCountScore = projectCountScore;
      this.workloadScore = workloadScore;
    }

    public String getUserId() {
      return userId;
    }

    public double getTotalScore() {
      return skillMatchScore + projectCountScore + workloadScore;
    }
  }
}