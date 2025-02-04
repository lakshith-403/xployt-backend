package com.xployt.dao.lead;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
// import java.sql.SQLException;
import java.util.List;
import com.xployt.util.DatabaseConfig;
import com.xployt.testProps.SetupUsers;
import com.xployt.testProps.SetupValidator;
import com.xployt.testProps.SetupProject;
// import java.util.logging.Logger;
// import com.xployt.util.CustomLogger;

public class ProjectValidatorsDAOTest {

  private ProjectValidatorsDAO projectValidatorsDAO;
  // private ProjectDAO projectDAO;
  private Connection conn;
  private static int TEST_CLIENT_ID;
  private static int TEST_PROJECT_LEAD_ID;
  private static int TEST_PROJECT_ID;
  private static int TEST_VALIDATOR_ID_1;
  private static int TEST_VALIDATOR_ID_2;
  // private static final Logger logger = CustomLogger.getLogger();

  @BeforeEach
  public void setUp() {
    try {
      conn = DatabaseConfig.getConnection();
      projectValidatorsDAO = new ProjectValidatorsDAO();
      // projectDAO = new ProjectDAO();
      TEST_CLIENT_ID = SetupUsers.makeTestClient("Test Client", "test.client@test.com", conn);
      TEST_PROJECT_ID = SetupProject.makeTestProject(TEST_CLIENT_ID, "Test Project", conn);
      SetupProject.addProjectScopeRandom(TEST_PROJECT_ID, 7, conn);
      TEST_PROJECT_LEAD_ID = SetupUsers.makeProjectLead("Test Project Lead", "test.projectlead@test.com", conn);
      TEST_VALIDATOR_ID_1 = SetupUsers.makeTestValidators("Test Validator 1", "test.validator1@test.com", conn);
      TEST_VALIDATOR_ID_2 = SetupUsers.makeTestValidators("Test Validator 2", "test.validator2@test.com", conn);
      SetupValidator.addValidatorInfoWithSkills(TEST_VALIDATOR_ID_1, 6, conn);
      SetupValidator.addValidatorInfoWithSkills(TEST_VALIDATOR_ID_2, 4, conn);
      System.out.println("Tests initialized successfully");
    } catch (Exception e) {
      System.out.println("Error occurred: " + e.getMessage());
      cleanUp();
    }
  }

  @Test
  public void placeHolderTest() {
    System.out.println("TEST_CLIENT_ID: " + TEST_CLIENT_ID);
    System.out.println("TEST_PROJECT_LEAD_ID: " + TEST_PROJECT_LEAD_ID);
    System.out.println("TEST_PROJECT_ID: " + TEST_PROJECT_ID);
    System.out.println("TEST_VALIDATOR_ID_1: " + TEST_VALIDATOR_ID_1);
    System.out.println("TEST_VALIDATOR_ID_2: " + TEST_VALIDATOR_ID_2);
  }

  @Test
  public void testAssignValidatorsBasedOnSkills() {
    int validatorCount = 2;

    try {
      List<String> assignedValidators = projectValidatorsDAO.assignValidatorsBasedOnSkills(TEST_PROJECT_ID,
          validatorCount, conn);

      // Basic validation
      if (assignedValidators == null) {
        fail("Assigned validators list should not be null");
      }
      if (assignedValidators.size() > validatorCount) {
        fail("Should not assign more validators than requested");
      }

      // Validate each assigned validator
      for (String validatorId : assignedValidators) {
        assertNotNull("Validator ID should not be null", validatorId);
        assertFalse(validatorId.isEmpty(), "Validator ID should not be empty");
      }

    } catch (Exception e) {
      fail("Error occurred: " + e.getMessage());
      cleanUp();
    }
  }

  @AfterEach
  public void tearDown() {
    cleanUp();
  }

  public void cleanUp() {
    try {
      if (conn != null) {
        conn = DatabaseConfig.getConnection();
      }
      try (var stmt = conn.createStatement()) {
        conn.setAutoCommit(true);
        // Clean up in correct order due to foreign key constraints
        System.out.println("CLEANING UP TEST DATA");
        stmt.execute("DELETE FROM ValidatorSkillSet WHERE validatorId IN (" + TEST_VALIDATOR_ID_1 + ", "
            + TEST_VALIDATOR_ID_2 + ")");
        stmt.execute("DELETE FROM ProjectValidators WHERE projectId IN (" + TEST_PROJECT_ID + ")");
        stmt.execute(
            "DELETE FROM ValidatorInfo WHERE validatorId IN (" + TEST_VALIDATOR_ID_1 + ", " + TEST_VALIDATOR_ID_2
                + ")");
        stmt.execute(
            "DELETE FROM ValidatorInfo WHERE validatorId IN (" + TEST_VALIDATOR_ID_1 + ", " + TEST_VALIDATOR_ID_2
                + ")");
        stmt.execute("DELETE FROM ProjectScope WHERE projectId IN (" + TEST_PROJECT_ID + ")");
        stmt.execute("DELETE FROM ProjectConfigs WHERE projectId IN (" + TEST_PROJECT_ID + ")");
        stmt.execute("DELETE FROM Projects WHERE projectId IN (" + TEST_PROJECT_ID + ")");
        stmt.execute("DELETE FROM Users WHERE userId IN (" + TEST_CLIENT_ID + ")");
        stmt.execute("DELETE FROM Users WHERE userId IN (" + TEST_VALIDATOR_ID_1 + ", " + TEST_VALIDATOR_ID_2 + ")");
        stmt.execute("DELETE FROM Users WHERE userId IN (" + TEST_PROJECT_LEAD_ID + ")");
        System.out.println("Deleted test users");
      } catch (Exception e) {
        fail("Error occurred while cleaning up: " + e.getMessage());
      }
    } catch (Exception e) {
      fail("Error occurred while cleaning up: " + e.getMessage());
    }
  }
}