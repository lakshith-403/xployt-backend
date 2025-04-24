package com.xployt.controller.common;

import com.xployt.dao.common.NotificationDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Notification;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/notifications/*")
public class NotificationsServlet extends HttpServlet {
    private NotificationDAO notificationDAO;
    @Override
    public void init() throws ServletException{
        notificationDAO = new NotificationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle GET requests for notifications
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        try{
            String userId = pathInfo.substring(1);
            List<Notification> notifications = notificationDAO.getNotificationsByUserId(userId);

            GenericResponse notificationResponse;

            if (notifications.isEmpty()) {
                notificationResponse = new GenericResponse(null, false, "No notifications found", null);
            } else {
                notificationResponse = new GenericResponse(notifications, true, "Notifications retrieved successfully", null);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.useGson().toJson(notificationResponse));

        } catch (SQLException e) {
            System.err.println("Error retrieving notifications" + e.getMessage());
        }

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        try{
            String userId = pathInfo.substring(1);
            notificationDAO.markAsRead(Integer.parseInt(userId));

            String data = "Notification marked as read successfully.";

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.useGson().toJson(data));

        } catch (SQLException e) {
            System.err.println("Error retrieving notifications" + e.getMessage());
        }
    }
}
