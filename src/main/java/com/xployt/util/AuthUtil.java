package com.xployt.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.xployt.dao.common.UserDAO;
import com.xployt.model.User;

public class AuthUtil {
    private static final Logger logger = CustomLogger.getLogger();
    public static boolean isSignedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    public static User getSignedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return null;
        }

        UserDAO userDAO = new UserDAO();
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user: {0}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Boolean isAdmin(HttpServletRequest request) {
        User user = getSignedInUser(request);
        return user != null && user.getRole().equals("admin");
    }

    public static Boolean isAllowedRole(HttpServletRequest request, String[] roles) {
        User user = getSignedInUser(request);
        return user != null && Arrays.asList(roles).contains(user.getRole());
    }

    public static Boolean isAllowedUser(HttpServletRequest request, String userId) {
        User user = getSignedInUser(request);
        return user != null && user.getUserId().equals(userId);
    }
}
