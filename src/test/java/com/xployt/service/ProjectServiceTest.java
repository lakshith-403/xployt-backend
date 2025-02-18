// package com.xployt.service;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
// import org.mockito.Mockito;

// import javax.servlet.ServletContext;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.StringReader;
// import java.sql.Connection;
// import java.io.PrintWriter;
// import java.io.StringWriter;

// import com.xployt.util.ContextManager;
// import com.xployt.service.client.ProjectService;
// import com.xployt.util.DatabaseConfig;
// import com.xployt.testProps.SetupUsers;

// public class ProjectServiceTest {

// private Connection conn;
// private ServletContext servletContext;
// private HttpServletRequest request;
// private HttpServletResponse response;
// private int TEST_CLIENT_ID;
// private int TEST_PROJECT_ID;

// @BeforeEach
// public void setUp() {
// request = Mockito.mock(HttpServletRequest.class);
// response = Mockito.mock(HttpServletResponse.class);
// servletContext = Mockito.mock(ServletContext.class);
// ContextManager.registerContext("DBConnection", servletContext);
// try {
// conn = DatabaseConfig.getConnection();
// } catch (Exception e) {
// e.printStackTrace();
// fail("Error getting connection: " + e.getMessage());
// }
// Mockito.when(servletContext.getAttribute("DBConnection")).thenReturn(conn);

// try {
// TEST_CLIENT_ID = SetupUsers.makeTestClientRandom("Test Client",
// "test@test.com", conn);
// } catch (Exception e) {
// e.printStackTrace();
// fail("Error making test client: " + e.getMessage());
// }

// // Mock request to return a BufferedReader
// String json = "{\"clientId\":\"" + TEST_CLIENT_ID
// + "\",\"title\":\"Test Project\",\"description\":\"This is a test
// project.\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-12-31\",\"url\":\"http://example.com\",\"technicalStack\":\"Java,
// Spring\"}";

// BufferedReader reader = new BufferedReader(new StringReader(json));
// try {
// Mockito.when(request.getReader()).thenReturn(reader);
// } catch (IOException e) {
// e.printStackTrace();
// }
// try {
// Mockito.when(response.getWriter()).thenReturn(new PrintWriter(new
// StringWriter()));
// } catch (IOException e) {
// e.printStackTrace();
// }
// }

// @Test
// public void testCreateProject() {

// ProjectService projectService = new ProjectService();
// try {
// TEST_PROJECT_ID = projectService.createProject(request, response);
// System.out.println("projectId: " + TEST_PROJECT_ID);
// } catch (Exception e) {
// e.printStackTrace();
// fail("IOException occurred: " + e.getMessage());
// }
// }

// @AfterEach
// public void tearDown() {
// System.out.println("Cleaning up test data");
// ContextManager.removeContext("DBConnection");
// try {
// if (conn != null) {
// conn = DatabaseConfig.getConnection();
// }
// } catch (Exception e) {
// System.out.println("Error getting connection: " + e.getMessage());
// }
// try (var stmt = conn.createStatement()) {
// conn.setAutoCommit(true);
// System.out.println("TEST_PROJECT_ID: " + TEST_PROJECT_ID);
// System.out.println("TEST_CLIENT_ID: " + TEST_CLIENT_ID);
// stmt.execute("DELETE FROM Projects WHERE projectId IN (" + TEST_PROJECT_ID +
// ")");
// stmt.execute("DELETE FROM Users WHERE userId IN (" + TEST_CLIENT_ID + ")");
// } catch (Exception e) {
// System.out.println("Error deleting test data: " + e.getMessage());
// }
// }
// }
