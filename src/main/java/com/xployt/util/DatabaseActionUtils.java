package com.xployt.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

public class DatabaseActionUtils {
  private static Connection conn = null;

  public static List<Map<String, Object>> executeSQL(String[] sqlStatements, List<Object[]> sqlParams)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, Object>> resultList = new ArrayList<>(); // ✅ Stores query results

    try {
      conn = DatabaseConfig.getConnection();
      conn.setAutoCommit(false);

      // Execute all statements except the last one if it's a SELECT
      for (int i = 0; i < sqlStatements.length - 1; i++) {

        try (PreparedStatement tempStmt = conn.prepareStatement(sqlStatements[i])) {
          tempStmt.setQueryTimeout(10); // Prevents long waits
          setParameters(tempStmt, sqlParams.get(i));

          if (!sqlStatements[i].trim().toLowerCase().startsWith("select")) {
            System.out.println("Executing INSERT/UPDATE/DELETE statement: " + sqlStatements[i]);
            tempStmt.executeUpdate();
          }
        } // ✅ `tempStmt` is automatically closed here
      }

      // Handle last statement (if it's a SELECT, store results)
      String lastSQL = sqlStatements[sqlStatements.length - 1];
      stmt = conn.prepareStatement(lastSQL);
      stmt.setQueryTimeout(10);
      setParameters(stmt, sqlParams.get(sqlStatements.length - 1));

      if (lastSQL.trim().toLowerCase().startsWith("select")) {
        System.out.println("Executing SELECT statement: " + lastSQL);
        rs = stmt.executeQuery();

        // ✅ Store all rows in resultList
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
          Map<String, Object> row = new HashMap<>();
          for (int i = 1; i <= columnCount; i++) {
            row.put(metaData.getColumnName(i), rs.getObject(i));
          }
          resultList.add(row);
        }
      }

      stmt.close(); // ✅ Now it's safe to close the statement
      conn.commit();
      return resultList; // ✅ Return stored result

    } catch (SQLException e) {
      System.out.println("SQL Error: " + e.getMessage());

      if (conn != null) {
        try {
          conn.rollback();
          System.out.println("Transaction rolled back due to an error.");
        } catch (SQLException ex) {
          throw new SQLException("Rollback failed: " + ex.getMessage());
        }
      }

      throw new SQLException("Database action failed: " + e.getMessage());
    } finally {
      try {
        if (rs != null)
          rs.close(); // ✅ Ensure ResultSet is closed
        if (conn != null)
          conn.close(); // ✅ Close connection after all operations
      } catch (SQLException ex) {
        System.out.println("Error closing resources: " + ex.getMessage());
      }
    }
  }

  private static void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
    try {
      if (params == null) {
        System.out.println("Warning: No parameters provided for statement.");
        return;
      }

      for (int i = 0; i < params.length; i++) {
        if (params[i] instanceof Integer) {
          stmt.setInt(i + 1, (Integer) params[i]);
        } else if (params[i] instanceof String) {
          stmt.setString(i + 1, (String) params[i]);
        } else if (params[i] instanceof Double) {
          stmt.setDouble(i + 1, (Double) params[i]);
        } else if (params[i] instanceof Boolean) {
          stmt.setBoolean(i + 1, (Boolean) params[i]);
        } else if (params[i] == null) {
          stmt.setNull(i + 1, java.sql.Types.NULL);
        } else {
          throw new SQLException("Unsupported parameter type: " + params[i].getClass().getName());
        }
      }
    } catch (Exception e) {
      throw new SQLException("Error setting parameters: " + e.getMessage());
    }
  }
}
