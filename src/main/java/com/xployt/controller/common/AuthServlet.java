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
import com.xployt.dao.common.RecoveryCodeDAO;
import com.xployt.dao.common.UserDAO;
import com.xployt.model.RecoveryCode;
import com.xployt.model.User;
import com.xployt.service.EmailService;
import com.xployt.util.AuthUtil;
import com.xployt.util.JsonUtil;
import com.xployt.util.PasswordUtil;
import com.xployt.util.ResponseProtocol;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private Map<String, HttpSession> sessions = new HashMap<>();

    @Override
    public void init() throws ServletException {
        // Initialize RecoveryCodes table if it doesn't exist
        try {
            RecoveryCodeDAO recoveryCodeDAO = new RecoveryCodeDAO();
            recoveryCodeDAO.createTableIfNotExists();
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize recovery codes table", e);
        }
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
            ResponseProtocol.sendSuccess(request, response, this, "User created successfully", userDataMap,
                    HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            ResponseProtocol.sendError(request, response, this, "Error creating user", e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        
        // Handle different endpoints
        if (path != null && path.startsWith("/password")) {
            handlePasswordEndpoint(request, response);
        } else {
            handleLogin(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
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

    @SuppressWarnings("unchecked")
    private void handlePasswordEndpoint(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();

        if (path.equals("/password/reset")) {
            // Handle password reset request (send recovery code)
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            Gson gson = JsonUtil.useGson();
            Map<String, String> requestData = gson.fromJson(requestBody, Map.class);
            String email = requestData.get("email");

            if (email == null || email.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, "Email is required", null,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            try {
                UserDAO userDAO = new UserDAO();
                User user = userDAO.getUserByEmail(email);
                
                if (user == null) {
                    // Don't reveal that the email doesn't exist for security reasons
                    ResponseProtocol.sendSuccess(request, response, this, 
                            "If your email is registered, a recovery code has been sent", null,
                            HttpServletResponse.SC_OK);
                    return;
                }
                
                // Generate and store recovery code
                RecoveryCodeDAO recoveryCodeDAO = new RecoveryCodeDAO();
                RecoveryCode recoveryCode = recoveryCodeDAO.createRecoveryCode(email);
                
                // Send email with recovery code
                EmailService emailService = new EmailService();
                String emailSubject = "Xployt Password Recovery";
                String emailBody = "Your password recovery code is: " + recoveryCode.getCode() + 
                        "\nThis code will expire in 5 minutes.";
                
                boolean emailSent = emailService.sendEmail(email, emailSubject, emailBody);
                
                if (emailSent) {
                    ResponseProtocol.sendSuccess(request, response, this, 
                            "Recovery code sent to your email", null,
                            HttpServletResponse.SC_OK);
                } else {
                    ResponseProtocol.sendError(request, response, this, 
                            "Failed to send recovery email", null,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                
            } catch (SQLException e) {
                ResponseProtocol.sendError(request, response, this, 
                        "Error processing recovery request", e.getMessage(),
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else if (path.equals("/password/change")) {
            // Handle password change (either with recovery code or for signed-in user)
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            Gson gson = JsonUtil.useGson();
            Map<String, String> requestData = gson.fromJson(requestBody, Map.class);
            
            String email = requestData.get("email");
            String newPassword = requestData.get("newPassword");
            String recoveryCode = requestData.get("recoveryCode");
            
            if (newPassword == null || newPassword.isEmpty()) {
                ResponseProtocol.sendError(request, response, this, 
                        "New password is required", null,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            try {
                UserDAO userDAO = new UserDAO();
                
                // Case 1: User is signed in - check session
                User signedInUser = AuthUtil.getSignedInUser(request);
                if (signedInUser != null) {
                    // Change password for signed-in user
                    String newPasswordHash = PasswordUtil.hashPassword(newPassword);
                    userDAO.updateUserPassword(signedInUser.getUserId(), newPasswordHash);
                    
                    ResponseProtocol.sendSuccess(request, response, this, 
                            "Password updated successfully", null, 
                            HttpServletResponse.SC_OK);
                    return;
                }
                
                // Case 2: Using recovery code
                if (email == null || email.isEmpty() || recoveryCode == null || recoveryCode.isEmpty()) {
                    ResponseProtocol.sendError(request, response, this, 
                            "Email and recovery code are required", null,
                            HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                User user = userDAO.getUserByEmail(email);
                if (user == null) {
                    ResponseProtocol.sendError(request, response, this, 
                            "Invalid email address", null,
                            HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                RecoveryCodeDAO recoveryCodeDAO = new RecoveryCodeDAO();
                RecoveryCode storedCode = recoveryCodeDAO.getByEmailAndCode(email, recoveryCode);
                
                if (storedCode != null && storedCode.isValid()) {
                    // Recovery code is valid, update password
                    String newPasswordHash = PasswordUtil.hashPassword(newPassword);
                    userDAO.updateUserPassword(user.getUserId(), newPasswordHash);
                    
                    // Delete the used recovery code
                    recoveryCodeDAO.deleteById(storedCode.getId());
                    
                    ResponseProtocol.sendSuccess(request, response, this, 
                            "Password updated successfully", null,
                            HttpServletResponse.SC_OK);
                } else {
                    ResponseProtocol.sendError(request, response, this, 
                            "Invalid or expired recovery code", null,
                            HttpServletResponse.SC_BAD_REQUEST);
                }
                
            } catch (SQLException e) {
                ResponseProtocol.sendError(request, response, this, 
                        "Error changing password", e.getMessage(),
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Gson gson = JsonUtil.useGson();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Logout successful");
        response.getWriter().write(gson.toJson(responseMap));
    }

    /*
     * Get the signed in user
     * Used by: All users at login
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n ------- AuthServlet | Get -------");

        User signedInUser = AuthUtil.getSignedInUser(request);
        if (signedInUser != null) {
            Gson gson = JsonUtil.useGson();

            System.out.println("Signed in user: " + signedInUser.getUserId());
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
            System.out.println("Not signed in");
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must log in
            // first");
            ResponseProtocol.sendError(request, response, this, "You must log in first", null,
                    HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
