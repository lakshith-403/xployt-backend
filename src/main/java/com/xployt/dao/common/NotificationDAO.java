package com.xployt.dao.common;

import com.xployt.model.Notification;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.util.ContextManager;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

           // Collect all users in a single list
           List<PublicUser> allUsers = new ArrayList<>();
           if (projectTeam.getClient() != null) {
               allUsers.add(projectTeam.getClient());
           }
           if (projectTeam.getProjectLead() != null) {
               allUsers.add(projectTeam.getProjectLead());
           }
           allUsers.addAll(projectTeam.getProjectValidators());
           allUsers.addAll(projectTeam.getProjectHackers());

           // Send notifications to all users
           sendNotificationToMultipleUsers(allUsers, message, projectId);
       } catch (Exception e) {
           System.err.println("Error sending notifications to team members: " + e.getMessage());
       }
   }

    public void sendNotificationToMultipleUsers(List<PublicUser> users, String message, int projectId) {
        try {
            for (PublicUser user : users) {
                Notification notification = new Notification(
                        Integer.parseInt(user.getUserId()),
                        "Project Update - #" + projectId,
                        message,
                        new java.sql.Timestamp(System.currentTimeMillis()),
                        false,
                        "/projects/" + projectId
                );
                createNotification(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error sending notifications to multiple users: " + e.getMessage());
        }
    }
}
