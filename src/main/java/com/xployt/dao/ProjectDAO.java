package com.xployt.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.xployt.model.Project;
import com.xployt.util.DatabaseConnection;


public class ProjectDAO {
    public List<Project> getAllProjects(String userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM test_project WHERE user_id = ?"; // Assuming a user_id column

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Project project = new Project(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getString("title"),
                        rs.getString("client"),
                        rs.getInt("pending_reports")
                );
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }
}
