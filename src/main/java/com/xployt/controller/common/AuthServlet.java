package com.xployt.controller.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.xployt.dao.common.UserDAO;
import com.xployt.model.User;
import com.xployt.util.AuthUtil;
import com.xployt.util.JsonUtil;
import com.xployt.util.PasswordUtil;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private Map<String, HttpSession> sessions = new HashMap<>();

    @Override
    public void init() throws ServletException {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Gson gson = JsonUtil.useGson();
        Map<String, String> userData = gson.fromJson(requestBody, Map.class);
        String email = userData.get("email");
        String password = userData.get("password");
        String name = userData.get("name");
        String role = userData.get("role");

        UserDAO userDAO = new UserDAO();
        User user = new User(null, email, PasswordUtil.hashPassword(password), name, role, null, null);
        try {
            User createdUser = userDAO.createUser(user);

            Map<String, Object> userDataMap = new HashMap<>();
            userDataMap.put("id", createdUser.getUserId());
            userDataMap.put("username", createdUser.getEmail());
            userDataMap.put("name", createdUser.getName());
            userDataMap.put("email", createdUser.getEmail());
            userDataMap.put("type", createdUser.getRole());
            userDataMap.put("avatar", "");
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", userDataMap);
            response.getWriter().write(gson.toJson(responseMap));

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating user");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Gson gson = JsonUtil.useGson();
        Map<String, String> credentials = gson.fromJson(requestBody, Map.class);
        String email = credentials.get("email");
        String password = credentials.get("password");

        UserDAO userDAO = new UserDAO();
        User user;
        try {
            user = userDAO.getUserByEmail(email);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching user");
            return;
        }

        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getUserId());
            sessions.put(session.getId(), session);

            Map<String, Object> userDataMap = new HashMap<>();
            userDataMap.put("id", user.getUserId());
            userDataMap.put("username", user.getEmail());
            userDataMap.put("name", user.getName());
            userDataMap.put("email", user.getEmail());
            userDataMap.put("type", user.getRole());
            userDataMap.put("avatar", "");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", userDataMap);
            response.getWriter().write(gson.toJson(responseMap));
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.getWriter().write("Logout successful");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User signedInUser = AuthUtil.getSignedInUser(request);
        if (signedInUser != null) {
            Gson gson = JsonUtil.useGson();
            Map<String, Object> userDataMap = new HashMap<>();
            userDataMap.put("id", signedInUser.getUserId());
            userDataMap.put("username", signedInUser.getEmail());
            userDataMap.put("name", signedInUser.getName());
            userDataMap.put("email", signedInUser.getEmail());
            userDataMap.put("type", signedInUser.getRole());
            userDataMap.put("avatar", "");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", userDataMap);
            response.getWriter().write(gson.toJson(responseMap));
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must log in first");
        }
    }
}
