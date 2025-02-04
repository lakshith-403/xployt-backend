package com.xployt.testProps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class SetupValidator {

  public static List<Integer> getRandomSkillIds(Connection conn, int count) throws Exception {
    String sql = "SELECT skillId FROM ValidatorSkills ORDER BY RAND() LIMIT ?";
    List<Integer> skillIds = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, count);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        skillIds.add(rs.getInt("skillId"));
      }
      return skillIds;
    } catch (SQLException e) {
      System.out.println("Error occurred: " + e.getMessage());
      throw e;
    }
  }

  public static void addValidatorInfo(int validatorId, Connection conn) {
    String sql = "INSERT INTO ValidatorInfo (validatorId, cvLink, experience) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, validatorId);
      stmt.setString(2, "https://example.com/cv");
      stmt.setString(3, "10 years");
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Error occurred: " + e.getMessage());
    }
  }

  public static void addValidatorInfoWithSkills(int validatorId, int count, Connection conn) throws Exception {
    addValidatorInfo(validatorId, conn);
    addValidatorSkills(validatorId, count, conn);
  }

  public static void addValidatorSkills(int validatorId, int count, Connection conn) throws Exception {
    try {
      List<Integer> skillIds = getRandomSkillIds(conn, count);
      String sql = "INSERT INTO ValidatorSkillSet (validatorId, skillId) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (int skillId : skillIds) {
          stmt.setInt(1, validatorId);
          stmt.setInt(2, skillId);
          stmt.executeUpdate();
        }
      } catch (SQLException e) {
        System.out.println("Error occurred: " + e.getMessage());
        throw e;
      }
    } catch (Exception e) {
      System.out.println("Error occurred: " + e.getMessage());
      throw e;
    }
  }
}
