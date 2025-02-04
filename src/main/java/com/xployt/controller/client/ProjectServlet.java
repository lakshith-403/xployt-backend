// package com.xployt.controller.client;

// import javax.servlet.annotation.WebServlet;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import javax.servlet.ServletException;
// import java.io.IOException;
// import com.xployt.service.client.ProjectService;

// @WebServlet("/api/client/project/")
// public class ProjectServlet extends HttpServlet {

// private final ProjectService projectService = new ProjectService();

// @Override
// protected void doPost(HttpServletRequest request, HttpServletResponse
// response) throws ServletException, IOException {
// response.setContentType("application/json");
// response.setCharacterEncoding("UTF-8");
// projectService.createProject(request, response);
// }
// }
