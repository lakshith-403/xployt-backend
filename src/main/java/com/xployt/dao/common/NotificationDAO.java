package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.xployt.model.Notification;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.util.ContextManager;

public class NotificationDAO {
    public NotificationDAO() {}

    public void createNotification(String userId, String title, String message, String url) throws SQLException {
        String sql = "INSERT INTO Notifications (userId, title, message, timestamp, isRead, url) VALUES (?, ?, ?, ?, ?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setString(2, title);
            stmt.setString(3, message);
            stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(5, false);
            stmt.setString(6, url);
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
    }
    
    public void createPaymentNotification(String userId, double amount, String description, double newBalance) throws SQLException {
        String title = "Payment Received";
        String message = String.format("You have received a payment of $%.2f. New balance: $%.2f", amount, newBalance);
        String url = "/dashboard";
        
        createNotification(userId, title, message, url);
        System.out.println("Payment notification sent to user: " + userId);
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
           sendNotificationToMultipleUsers(allUsers, "Project Update - #" + projectId, message, "/projects/" + projectId);
       } catch (Exception e) {
           System.err.println("Error sending notifications to team members: " + e.getMessage());
       }
   }

    public void sendNotificationToMultipleUsers(List<PublicUser> users, String title, String message, String url) throws SQLException {
        try {
            for (PublicUser user : users) {
                createNotification(
                    user.getUserId(),
                    title,
                    message,
                    url
                );
            }
        } catch (SQLException e) {
            System.err.println("Error sending notifications to multiple users: " + e.getMessage());
        }
    }

    public List<Notification> getNotificationsByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM Notifications WHERE userId = ? ORDER BY timestamp DESC";
        List<Notification> notifications = new ArrayList<>();

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(userId));
            var rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setUserId(rs.getInt("userId"));
                notification.setTitle(rs.getString("title"));
                notification.setMessage(rs.getString("message"));
                notification.setTimestamp(rs.getTimestamp("timestamp"));
                notification.setRead(rs.getBoolean("isRead"));
                notification.setUrl(rs.getString("url"));
                notifications.add(notification);
            }

            System.out.println("No of Notifications for user " + userId + " : " + notifications.size());
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            throw e;
        }

        return notifications;
    }

    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE Notifications SET isRead = true WHERE id = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            var rs = stmt.executeUpdate();

            if (rs > 0) {
                System.out.println("Notification marked as read successfully");
            }
        }catch (SQLException e){
            System.err.println("SQL Exception: " + e.getMessage());
            throw e;
        }
    }
}
