package com.xployt.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class ResponseUtil {

  private static final Logger logger = CustomLogger.getLogger();

  public static void writeResponse(HttpServletResponse response, String content) {
    try {
      response.getWriter().write(content);
    } catch (IOException e) {
      logger.severe("Error writing to response: " + e.getMessage());
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
      } catch (IOException ex) {
        logger.severe("Error sending error response: " + ex.getMessage());
      }
    }
  }
}