package com.xployt.dao.lead;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.xployt.util.DatabaseTestConfig;
// import java.util.logging.Logger;
// import com.xployt.util.CustomLogger;

public class ProjectValidatorsDAOTest {

  private ProjectValidatorsDAO projectValidatorsDAO;
  private Connection conn;
  private static final String TEST_PROJECT_ID = "123";
  // private static final Logger logger = CustomLogger.getLogger();

  @Before
  public void setUp() throws SQLException {
    conn = DatabaseTestConfig.getConnection();
    projectValidatorsDAO = new ProjectValidatorsDAO();
    System.out.println("Tests initialized successfully");

  }

  @After
  public void tearDown() throws SQLException {
    try (var stmt = conn.createStatement()) {
      // Clean up in correct order due to foreign key constraints
      stmt.execute("DELETE FROM Users WHERE email LIKE 'test.validator%'");
      System.out.println("Deleted test users");
      stmt.execute(
          "DELETE FROM ProjectValidators WHERE projectId IN (SELECT projectId FROM Projects WHERE title = 'Test Project')");
      stmt.execute(
          "DELETE FROM ProjectScope WHERE projectId IN (SELECT projectId FROM Projects WHERE title = 'Test Project')");
      stmt.execute(
          "DELETE FROM ProjectConfigs WHERE projectId IN (SELECT projectId FROM Projects WHERE title = 'Test Project')");
      stmt.execute("DELETE FROM Projects WHERE title = 'Test Project'");
    }
  }

  @Test
  public void testAssignValidatorsBasedOnSkills() {
    int validatorCount = 2;

    try {
      List<String> assignedValidators = projectValidatorsDAO.assignValidatorsBasedOnSkills(TEST_PROJECT_ID,
          validatorCount);

      // Basic validation
      assertNotNull("Assigned validators list should not be null",
          assignedValidators);
      assertTrue("Should not assign more validators than requested",
          assignedValidators.size() <= validatorCount);

      // Validate each assigned validator
      for (String validatorId : assignedValidators) {
        assertNotNull("Validator ID should not be null", validatorId);
        assertFalse("Validator ID should not be empty", validatorId.isEmpty());
      }

    } catch (SQLException e) {
      fail("Error occurred: " + e.getMessage());
    }
  }

  // @Test(expected = SQLException.class)
  // public void testAssignValidatorsBasedOnSkills_InvalidProjectId() throws
  // SQLException {
  // // Test with invalid project ID
  // projectValidatorsDAO.assignValidatorsBasedOnSkills("invalid_project_id", 2);
  // }

  // @Test
  // public void testAssignValidatorsBasedOnSkills_ZeroValidators() {
  // try {
  // List<String> assignedValidators =
  // projectValidatorsDAO.assignValidatorsBasedOnSkills(TEST_PROJECT_ID, 0);
  // assertTrue("Should return empty list for zero validators",
  // assignedValidators.isEmpty());
  // } catch (SQLException e) {
  // fail("Should not throw SQLException: " + e.getMessage());
  // }
  // }
  @Test
  public void InsertTestUsers() {
    try {
      // Insert users
      try (var insertStmt = conn.createStatement()) {
        System.out.println("Inserting test users...");
        insertStmt.execute(
            "INSERT INTO Users (email, passwordHash, name, role) VALUES " +
                "('test.validator1@test.com', 'hash1', 'Test Validator 1', 'VALIDATOR')," +
                "('test.validator2@test.com', 'hash2', 'Test Validator 2', 'VALIDATOR')");
      }

      // Verify insertion
      try (var selectStmt = conn.createStatement();
          var rs = selectStmt.executeQuery("SELECT * FROM Users WHERE email LIKE 'test.validator%'")) {
        System.out.println("Inserted users:");
        while (rs.next()) {
          System.out.println(String.format("ID: %s, Email: %s, Name: %s, Role: %s",
              rs.getString("userId"),
              rs.getString("email"),
              rs.getString("name"),
              rs.getString("role")));
        }
      }
    } catch (SQLException e) {
      System.out.println("Error inserting test users: " + e.getMessage());
    }
  }
}