package com.xployt.service.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import java.io.BufferedReader;

import com.xployt.util.CustomLogger;
import com.xployt.dao.common.ProfileDAO;
import com.xployt.util.JsonUtil;
import com.xployt.model.GenericResponse;
import com.xployt.util.ResponseUtil;
import com.xployt.model.Profile;

public class ProfileService {

  private static final Logger logger = CustomLogger.getLogger();

  public void getProfileInfo(HttpServletRequest request, HttpServletResponse response) {

    logger.info("ProfileService getProfileInfo method called");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      ResponseUtil.writeResponse(response,
          JsonUtil.toJson(new GenericResponse(null, false, "User ID not provided hehe hoo", null)));
      return;
    }
    int userId = Integer.parseInt(pathInfo.substring(1));
    
    ProfileDAO profileDAO = new ProfileDAO();
    try {
      Profile result = profileDAO.getProfile(userId);
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(result, true, null, null)));

    } catch (Exception e) {
      ResponseUtil.writeResponse(response, JsonUtil.toJson(new GenericResponse(null, false, null, null)));
    }
  }

  public void updateProfileInfo(HttpServletRequest request, HttpServletResponse response) {
    logger.info("ProfileService updateProfileInfo method called");
    
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      ResponseUtil.writeResponse(response,
          JsonUtil.toJson(new GenericResponse(null, false, "User ID not provided", null)));
      return;
    }

    int userId = Integer.parseInt(pathInfo.substring(1));
    ProfileDAO profileDAO = new ProfileDAO();
    
    try {
      StringBuilder jsonBody = new StringBuilder();
      String line;
      try (BufferedReader reader = request.getReader()) {
        while ((line = reader.readLine()) != null) {
          jsonBody.append(line);
        }
      }
      
      Profile profile = JsonUtil.fromJson(jsonBody.toString(), Profile.class);
      profile.setUserId(userId);
      
      boolean result = profileDAO.updateProfile(profile);
      ResponseUtil.writeResponse(response, 
          JsonUtil.toJson(new GenericResponse(result, true, null, null)));

    } catch (Exception e) {
      logger.severe("Error updating profile: " + e.getMessage());
      ResponseUtil.writeResponse(response, 
          JsonUtil.toJson(new GenericResponse(null, false, "Error updating profile", null)));
    }
  }
}
