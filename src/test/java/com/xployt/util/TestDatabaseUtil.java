// package com.xployt.util;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;
// import java.io.InputStream;
// import java.util.Properties;

// public class TestDatabaseUtil {

// public static Connection getTestConnection() throws Exception {
// Properties props = new Properties();
// try (InputStream is =
// TestDatabaseUtil.class.getResourceAsStream("/test-db.properties")) {
// props.load(is);
// }
// return DriverManager.getConnection(
// props.getProperty("db.url"),
// props.getProperty("db.username"),
// props.getProperty("db.password"));
// }

// public static void setupTestData(Connection conn) throws SQLException {
// // Create temporary test data
// try (var stmt = conn.createStatement()) {
// // Add test validators
// stmt.execute(
// "INSERT INTO Users (userId, name, role) VALUES ('test_validator1', 'Test
// Validator 1', 'VALIDATOR')");
// stmt.execute(
// "INSERT INTO Users (userId, name, role) VALUES ('test_validator2', 'Test
// Validator 2', 'VALIDATOR')");
// // Add test project
// stmt.execute("INSERT INTO Projects (projectId, name) VALUES ('project123',
// 'Test Project')");
// }
// }

// public static void cleanTestData(Connection conn) throws SQLException {
// // Clean up test data
// try (var stmt = conn.createStatement()) {
// stmt.execute("DELETE FROM ProjectValidators WHERE projectId = 'project123'");
// stmt.execute("DELETE FROM Projects WHERE projectId = 'project123'");
// stmt.execute("DELETE FROM Users WHERE userId LIKE 'test_validator%'");
// }
// }
// }