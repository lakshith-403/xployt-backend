package com.xployt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger logger = CustomLogger.getLogger();
    private static Connection connection;
    private static final String URL = "jdbc:mysql://xployt-xployt.b.aivencloud.com:17847/xployt?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_5G4ol30FyzBOm-NNf6x";

    public static Connection getConnection() {
        if (connection == null) {
            initializeConnection();
        }
        return connection;
    }

    private static void initializeConnection() {
        try {
            // Use H2 in-memory database for testing
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD);

            logger.log(Level.INFO, "Test database connection initialized");

        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "H2 driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to initialize test database", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize test database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.log(Level.INFO, "Test database connection closed");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing test database connection: " + e.getMessage());
            }
        }
    }
}