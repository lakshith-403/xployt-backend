package com.xployt.middleware;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.util.JsonUtil;

public class GlobalExceptionHandler extends HttpServlet {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        if (throwable == null) {
            throwable = (Throwable) request.getAttribute("javax.servlet.error.exception_type");
        }
        System.out.println(request);
        // Get the root cause of the exception
        Throwable rootCause = getRootCause(throwable);
        
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        logger.log(Level.SEVERE, "Error in servlet: " + servletName + " for URI: " + requestUri, rootCause);

        response.setContentType("application/json");
        response.setStatus(statusCode != null ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("code", statusCode);
        errorResponse.put("uri", requestUri);
        errorResponse.put("servlet", servletName);
        errorResponse.put("message", errorMessage);

        if (rootCause != null) {
            errorResponse.put("message", rootCause.getMessage());
            errorResponse.put("type", rootCause.getClass().getSimpleName());
            errorResponse.put("detail", getFullErrorMessage(rootCause));
            
            // Include stack trace in development environment
            StackTraceElement[] stackTrace = rootCause.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                errorResponse.put("stackTrace", stackTrace[0].toString());
            }
        } else if (errorMessage != null) {
            errorResponse.put("message", errorMessage);
            errorResponse.put("type", "UnknownError");
        } else {
            errorResponse.put("message", "Unknown error occurred");
            errorResponse.put("type", "UnknownError");
        }

        String jsonResponse = JsonUtil.toJson(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private Throwable getRootCause(Throwable throwable) {
        if (throwable == null) return null;
        
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    private String getFullErrorMessage(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (sb.length() > 0) {
                sb.append(" | Caused by: ");
            }
            sb.append(current.getMessage());
            current = current.getCause();
        }
        return sb.toString();
    }
} 