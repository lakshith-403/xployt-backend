package com.xployt.controller.validator;

import com.xployt.service.validator.ValidatorService;
import com.xployt.util.CustomLogger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/api/validator/promote/*")
public class PromoteValidatorServlet extends HttpServlet {
    private static final Logger logger = CustomLogger.getLogger();
    private final ValidatorService validatorService = new ValidatorService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("PromoteValidatorServlet doPost method called");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            logger.warning("PromoteValidatorServlet: User ID not provided");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        String userId = pathInfo.substring(1); // Extract userId from the URL
        try {
            boolean isPromoted = validatorService.promoteValidatorToProjectLead(userId);
            if (isPromoted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\": \"User promoted to Project Lead successfully.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error promoting user.");
            }
        } catch (Exception e) {
            logger.severe("Error promoting validator: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error promoting user.");
        }
    }
} 