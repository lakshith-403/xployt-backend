package com.xployt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://xployt-xployt.b.aivencloud.com:17847/xployt?ssl-mode=REQUIRED"; // Your
                                                                                                                    // database
                                                                                                                    // URL
    private static final String USER = "avnadmin"; // Your database username
    private static final String PASSWORD = "AVNS_nwmduVxzhIqTj911GGT"; // Your database password
    private static final Logger logger = CustomLogger.getLogger();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
            logger.log(Level.INFO, "Connection established successfully!");

        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed.");
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }
}
