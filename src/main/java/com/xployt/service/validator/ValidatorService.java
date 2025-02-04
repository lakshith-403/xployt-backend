package com.xployt.service.validator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.dao.common.UserDAO;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import java.io.IOException;
import java.util.logging.Logger;

public class ValidatorService {
    private final UserDAO userDAO = new UserDAO();
    private static final Logger logger = CustomLogger.getLogger();

    public void promoteValidatorToProjectLead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            logger.severe("ValidatorService: User ID not provided");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        String userId = pathInfo.substring(1); // Extract userId from the URL
        logger.info("ValidatorService: Promoting User ID: " + userId);

        try {
            boolean isPromoted = userDAO.updateUserRole(userId, "Project Lead");
            if (isPromoted) {
                logger.info("ValidatorService: User promoted to Project Lead successfully.");
                response.getWriter().write(JsonUtil.toJson(new GenericResponse(null, true, "User promoted to Project Lead successfully.", null)));
            } else {
                logger.severe("ValidatorService: Error promoting user.");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error promoting user.");
            }
        } catch (Exception e) {
            logger.severe("ValidatorService: Exception occurred while promoting user: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error promoting user.");
        }
    }
} 