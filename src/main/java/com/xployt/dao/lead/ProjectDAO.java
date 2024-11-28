package com.xployt.dao.lead;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class ProjectDAO {

  private static final Logger logger = CustomLogger.getLogger();

  public void updateProjectStatus(String projectId, String status) throws SQLException {
    String sql = "UPDATE Projects SET status = ? WHERE projectId = ?";
    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");
    logger.info("ProjectDAO: Connection established");

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, status);
      stmt.setString(2, projectId);
      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        logger.info("ProjectDAO: Project status updated successfully for projectId: " + projectId);
      } else {
        logger.warning("ProjectDAO: No project found with projectId: " + projectId);
      }
    } catch (SQLException e) {
      logger.severe("ProjectDAO: Error updating project status: " + e.getMessage());
      throw e;
    }
  }
}