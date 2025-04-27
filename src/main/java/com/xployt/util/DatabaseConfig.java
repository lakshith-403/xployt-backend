package com.xployt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {
    private static final Logger logger = CustomLogger.getLogger();
    private static Connection connection;
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // ✅ Always return a new connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found", e);
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