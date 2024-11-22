package com.xployt.controller.common;

import com.xployt.service.common.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;

@WebServlet("/api/user/profile/*") 
public class Profile extends HttpServlet {
    private ProfileService profileService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        profileService = new ProfileService();
    }

      
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    logger.info("Profile doGet method called");
    profileService.getProfileInfo(request, response);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    logger.info("Profile doPost method called");
    profileService.updateProfileInfo(request, response);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
  }
}
