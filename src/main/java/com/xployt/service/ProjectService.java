package com.xployt.service;
import com.xployt.dao.ProjectDAO;
import com.xployt.model.Project;
import java.util.List;

public class ProjectService {
    private ProjectDAO projectDAO;

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public List<Project> fetchProjects(String userId) {
        return projectDAO.getAllProjects(userId);
    }
}
