package com.xployt.dao.common;

import com.xployt.model.Notification;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
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

   public void sendNotificationsToProjectTeam(int projectId, String message) {
        try {
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(String.valueOf(projectId));
            NotificationDAO notificationDAO = new NotificationDAO();

            prepareAndSendNotification(notificationDAO, projectTeam.getClient(), message, projectId);
            prepareAndSendNotification(notificationDAO, projectTeam.getProjectLead(), message, projectId);

            projectTeam.getProjectValidators().forEach(validator -> sendNotificationToUser(notificationDAO, validator, message, projectId));
            projectTeam.getProjectHackers().forEach(hacker -> sendNotificationToUser(notificationDAO, hacker, message, projectId));
        } catch (Exception e) {
            System.err.println("Error sending notifications to team members: " + e.getMessage());
        }
    }

    private void sendNotificationToUser(NotificationDAO notificationDAO, PublicUser user, String message, int projectId) {
        try {
            prepareAndSendNotification(notificationDAO, user, message, projectId);
        } catch (SQLException e) {
            System.err.println("Error sending notification to user: " + e.getMessage());
        }
    }

    private void prepareAndSendNotification(NotificationDAO notificationDAO, PublicUser user, String message, int projectId) throws SQLException {
        if (user != null) {
            Notification notification = new Notification(
                    Integer.parseInt(user.getUserId()),
                    "Project Update - #" + projectId,
                    message,
                    new java.sql.Timestamp(System.currentTimeMillis()),
                    false,
                    "/projects/" + projectId
            );
            notificationDAO.createNotification(notification);
        }
    }
}
