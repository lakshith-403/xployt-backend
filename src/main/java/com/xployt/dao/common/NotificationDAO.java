package com.xployt.dao.common;

import com.xployt.model.Notification;
import com.xployt.util.ContextManager;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificationDAO {
    public NotificationDAO() {}

    public Notification createNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO Notifications (userId, title, message, timestamp, isRead, url) VALUES (?, ?, ?, ?, ?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setTimestamp(4, new java.sql.Timestamp(notification.getTimestamp().getTime()));
            stmt.setBoolean(5, notification.isRead());
            stmt.setString(6, notification.getUrl());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0){
                System.out.println("Notification recorded successfully");
            } else {
                throw new SQLException("Creating notification failed, no rows affected.");
            }
        }catch (SQLException e){
            System.err.println("SQL Exception: " + e.getMessage());
            throw e;
        }
        return notification;
    }
}
