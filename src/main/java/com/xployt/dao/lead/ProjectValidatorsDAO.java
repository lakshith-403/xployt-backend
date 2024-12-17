package com.xployt.dao.lead;

import com.xployt.util.CustomLogger;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import com.xployt.util.ContextManager;

public class ProjectValidatorsDAO {
  private static final Logger logger = CustomLogger.getLogger();

  // Weight constants for different factors
  private static final double SKILL_MATCH_WEIGHT = 0.5; // 50% importance
  private static final double PROJECT_COUNT_WEIGHT = 0.3; // 30% importance
  private static final double WORKLOAD_BALANCE_WEIGHT = 0.2; // 20% importance

  public List<String> assignValidatorsBasedOnSkills(String projectId, int validatorCount) throws SQLException {
    List<String> assignedValidators = new ArrayList<>();

    String validatorSelectionSQL = "WITH ProjectScopes AS (" +
        "    SELECT DISTINCT s.skill_id " +
        "    FROM project_scopes ps " +
        "    JOIN scope_to_skills s2s ON ps.scope_id = s2s.scope_id " +
        "    WHERE ps.project_id = ?" +
        "), " +
        "ValidatorScores AS (" +
        "    SELECT " +
        "        v.userId, " +
        "        -- Calculate skill match score (percentage of project required skills matched by validator)" +
        "        (COUNT(DISTINCT CASE WHEN vs.skill_id IN (SELECT skill_id FROM ProjectScopes) THEN vs.skill_id END) / "
        +
        "         (SELECT COUNT(*) FROM ProjectScopes)) * ? as skill_match_score, " +
        "        -- Calculate project count score (inverse of current project count)" +
        "        (1 - (COUNT(DISTINCT pv.projectId) / (SELECT MAX(project_count) + 1 " +
        "                                              FROM (SELECT COUNT(*) as project_count " +
        "                                                    FROM ProjectValidators " +
        "                                                    GROUP BY validatorId) counts))) * ? as project_count_score, "
        +
        "        -- Calculate workload balance score" +
        "        (1 - (COUNT(DISTINCT CASE WHEN pv.status = 'Active' THEN pv.projectId END) / " +
        "              (SELECT MAX(active_count) + 1 " +
        "               FROM (SELECT COUNT(*) as active_count " +
        "                     FROM ProjectValidators " +
        "                     WHERE status = 'Active' " +
        "                     GROUP BY validatorId) counts))) * ? as workload_score " +
        "    FROM Users v " +
        "    LEFT JOIN validator_skills vs ON v.userId = vs.validator_id " +
        "    LEFT JOIN ProjectValidators pv ON v.userId = pv.validatorId " +
        "    WHERE v.role = 'VALIDATOR' " +
        "    GROUP BY v.userId " +
        ") " +
        "SELECT " +
        "    userId, " +
        "    (skill_match_score + project_count_score + workload_score) as total_score " +
        "FROM ValidatorScores " +
        "WHERE userId NOT IN (SELECT validatorId FROM ProjectValidators WHERE projectId = ?) " +
        "ORDER BY total_score DESC " +
        "LIMIT ?";

    String insertSQL = "INSERT INTO ProjectValidators (projectId, validatorId, status) VALUES (?, ?, 'Pending')";

    Connection conn = (Connection) ContextManager.getContext("DBConnection").getAttribute("DBConnection");
    try {
      conn.setAutoCommit(false);

      // Select validators based on weighted scores
      try (PreparedStatement selectStmt = conn.prepareStatement(validatorSelectionSQL)) {
        selectStmt.setString(1, projectId);
        selectStmt.setDouble(2, SKILL_MATCH_WEIGHT);
        selectStmt.setDouble(3, PROJECT_COUNT_WEIGHT);
        selectStmt.setDouble(4, WORKLOAD_BALANCE_WEIGHT);
        selectStmt.setString(5, projectId);
        selectStmt.setInt(6, validatorCount);

        ResultSet rs = selectStmt.executeQuery();

        // Insert selected validators
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
          while (rs.next()) {
            String validatorId = rs.getString("userId");
            insertStmt.setString(1, projectId);
            insertStmt.setString(2, validatorId);
            insertStmt.executeUpdate();
            assignedValidators.add(validatorId);

            logger.info(String.format(
                "Assigned validator %s to project %s with score %f",
                validatorId, projectId, rs.getDouble("total_score")));
          }
        }
      }

      conn.commit();
      conn.close();
      return assignedValidators;

    } catch (SQLException e) {

      conn.rollback();
      conn.close();
      logger.severe("Error assigning validators: " + e.getMessage());
      throw e;
    }
  }
}