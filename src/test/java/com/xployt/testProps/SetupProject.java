package com.xployt.testProps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.xployt.dao.client.ProjectDAO;

public class SetupProject {

  public static int makeTestProject(int clientId, String projectTitle, Connection conn) {
    ProjectDAO projectDAO = new ProjectDAO();
    try {
      int projectId = projectDAO.createProject(
          clientId,
          projectTitle,
          "This is a test project",
          "2023-01-01",
          "2023-12-31",
          "http://example.com",
          "Java, Spring",
          conn);
      return projectId;
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating test project: " + e.getMessage());
      return -1;
    }
  }

  public static int makeTestProject(int clientId, String projectTitle, String projectDescription, String startDate,
      String endDate, String url, String technicalStack, Connection conn) {
    ProjectDAO projectDAO = new ProjectDAO();
    try {
      int projectId = projectDAO.createProject(
          clientId,
          projectTitle,
          projectDescription,
          startDate,
          endDate,
          url,
          technicalStack,
          conn);
      return projectId;
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error creating test project: " + e.getMessage());
      return -1;
    }
  }

  public static void addProjectScope(int projectId, int scopeId, Connection conn) {
    String sql = "INSERT INTO ProjectScope (projectId, scopeId) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, projectId);
      stmt.setInt(2, scopeId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static List<Integer> getProjectScopeIdsRandom(Connection conn, int limit) {
    String sql = "SELECT scopeId FROM ProjectScope ORDER BY RAND() LIMIT ?";
    List<Integer> scopeIds = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, limit);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        scopeIds.add(rs.getInt("scopeId"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return scopeIds;
  }

  public static void addProjectScopeRandom(int projectId, int count, Connection conn) {
    List<Integer> scopeIds = getProjectScopeIdsRandom(conn, count);
    int length = scopeIds.size();
    String sql = "INSERT INTO ProjectScope (projectId, scopeId) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, projectId);
      for (int i = 0; i < length; i++) {
        stmt.setInt(2, scopeIds.get(i));
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
