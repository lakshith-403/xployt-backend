package com.xployt.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DatabaseActionUtils {

  // private static List<Map<String, Object>> resultList = new ArrayList<>(); // ✅
  // Stores query results

  public static List<Map<String, Object>> executeSQL(String[] sqlStatements, List<Object[]> sqlParams)
      throws SQLException {
    PreparedStatement stmt = null;
    // PreparedStatement tempStmt = null;
    // resultList.clear();
    List<Map<String, Object>> resultList = new ArrayList<>();
    ResultSet rs = null;

    // System.out.println("------ Executing SQL statements --------");
    // System.out.println("Number of statements: " + sqlStatements.length);

    Connection conn = null;

    try {
      conn = DatabaseConfig.getConnection();
      conn.setAutoCommit(false);

      int lastIndex = sqlStatements.length - 1;

      // ✅ Check if there's a `SELECT` before the last statement
      for (int i = 0; i < lastIndex; i++) {
        if (sqlStatements[i].trim().toLowerCase().startsWith("select")) {
          // System.out.println("ERROR: SELECT statement found in the middle or start.
          // Aborting.");
          throw new SQLException("SELECT statements must be at the end of the execution batch.");
        }
      }

      // ✅ Execute all non-SELECT statements (INSERT, UPDATE, DELETE)
      for (int i = 0; i < lastIndex; i++) {
        try {
          stmt = conn.prepareStatement(sqlStatements[i]);
          System.out.println("Executing non-SELECT statement: " + sqlStatements[i]);
          // stmt.setQueryTimeout(10); // Prevents long waits
          setParameters(stmt, sqlParams.get(i));
          stmt.executeUpdate(); // No need to store the result

        } catch (SQLException e) {
          // System.out.println("SQL Error: " + e.getMessage());
          throw new SQLException("Database action failed at statement: " + sqlStatements[i] + ": "
              + e.getMessage());
        }
      }

      // ✅ Handle last statement
      String lastSQL = sqlStatements[lastIndex].trim().toLowerCase();

      if (lastSQL.startsWith("select")) {
        System.out.println("Executing SELECT statement: " +
            sqlStatements[lastIndex]);
        // ✅ If last statement is a SELECT, execute and return results
        stmt = conn.prepareStatement(sqlStatements[lastIndex]);
        // stmt.setQueryTimeout(10);
        // System.out.println("SQL Params size: " + sqlParams.size());
        if (sqlParams.size() > 0 && sqlParams.get(lastIndex) != null) {
          setParameters(stmt, sqlParams.get(lastIndex));
        }

        // System.out.println("Executing SELECT statement parameters injected");
        rs = stmt.executeQuery();
        // System.out.println("Results: " + rs);
        resultList = getResults(rs, resultList);

      } else {
        stmt = conn.prepareStatement(sqlStatements[lastIndex]);
        // stmt.setQueryTimeout(10);
        setParameters(stmt, sqlParams.get(lastIndex));
        System.out.println("Executing non-SELECT statement: " + sqlStatements[lastIndex]);
        stmt.executeUpdate();

      }

      conn.commit();
      // System.out.println("SQL executed successfully");

      return resultList; // ✅ Return result only if SELECT was executed

    } catch (

    SQLException e) {
      // System.out.println("SQL Error: " + e.getMessage());

      if (conn != null) {
        try {
          conn.rollback();
          // System.out.println("Transaction rolled back due to an error.");
        } catch (SQLException ex) {
          throw new SQLException("Rollback failed: " + ex.getMessage());
        }
      }

      throw new SQLException("Database action failed: " + e.getMessage());
    } finally {
      try {
        // System.out.println("Closing resources");
        if (rs != null)
          rs.close(); // ✅ Ensure ResultSet is closed
        if (stmt != null)
          stmt.close(); // ✅ Ensure statement is closed
        // if (tempStmt != null)
        // tempStmt.close(); // ✅ Ensure statement is closed
        if (conn != null)
          conn.close(); // ✅ Close connection after all operations

      } catch (SQLException ex) {
        // System.out.println("Error closing resources: " + ex.getMessage());
      }
    }
  }

  public static List<Map<String, Object>> getResults(ResultSet rs, List<Map<String, Object>> resultList)
      throws SQLException {
    // ✅ Store all rows in resultList
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    while (rs.next()) {
      Map<String, Object> row = new HashMap<>();
      for (int i = 1; i <= columnCount; i++) {
        Object value = rs.getObject(i);
        if (value == null) {
          value = "";
        }
        row.put(metaData.getColumnName(i), value);
      }
      resultList.add(row);
    }

    return resultList;
  }

  public static void executeBatchSQL(String sql, List<Object[]> batchParams) throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;

    System.out.println("Executing batch SQL: " + sql);
    
    try {
      conn = DatabaseConfig.getConnection();
      conn.setAutoCommit(false); // Start transaction

      stmt = conn.prepareStatement(sql);

      for (Object[] params : batchParams) {
        setParameters(stmt, params);
        stmt.addBatch();
      }

      stmt.executeBatch(); // Execute batch
      conn.commit(); // Commit transaction

    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback(); // Rollback transaction on error
        } catch (SQLException ex) {
          throw new SQLException("Rollback failed: " + ex.getMessage());
        }
      }
      throw new SQLException("Batch execution failed: " + e.getMessage());
    } finally {
      try {
        if (stmt != null)
          stmt.close();
        if (conn != null) {
          // System.out.println("Closing connection");
          // conn.close();
        }
      } catch (SQLException ex) {
        // Log error closing resources
      }
    }
  }

  private static void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
    if (params == null)
      return;

    StringBuilder paramLog = new StringBuilder("SQL Parameters: [");
    for (int i = 0; i < params.length; i++) {
      if (i > 0) paramLog.append(", ");
      
      if (params[i] instanceof Integer) {
        stmt.setInt(i + 1, (Integer) params[i]);
        paramLog.append("Int: ").append(params[i]);
      } else if (params[i] instanceof String) {
        stmt.setString(i + 1, (String) params[i]);
        paramLog.append("String: '").append(params[i]).append("'");
      } else if (params[i] instanceof Double) {
        stmt.setDouble(i + 1, (Double) params[i]);
        paramLog.append("Double: ").append(params[i]);
      } else if (params[i] instanceof Boolean) {
        stmt.setBoolean(i + 1, (Boolean) params[i]);
        paramLog.append("Boolean: ").append(params[i]);
      } else if (params[i] instanceof java.sql.Date) {
        stmt.setDate(i + 1, (java.sql.Date) params[i]);
        paramLog.append("SqlDate: ").append(params[i]);
      } else if (params[i] instanceof java.util.Date) {
        java.sql.Date sqlDate = new java.sql.Date(((java.util.Date) params[i]).getTime());
        stmt.setDate(i + 1, sqlDate);
        paramLog.append("UtilDate: ").append(sqlDate);
      } else if (params[i] == null) {
        stmt.setNull(i + 1, java.sql.Types.NULL);
        paramLog.append("NULL");
      } else {
        String typeName = params[i].getClass().getName();
        paramLog.append("UNSUPPORTED(").append(typeName).append("): ").append(params[i]);
        System.err.println("Unsupported parameter type: " + typeName + " for parameter at index " + (i + 1));
        throw new SQLException("Unsupported parameter type: " + typeName);
      }
    }
    paramLog.append("]");
    System.out.println(paramLog.toString());
  }

  public static List<Map<String, Object>> callProcedure(String procedureCall, Object[] params)
    throws SQLException {

  List<Map<String, Object>> resultList = new ArrayList<>();
  ResultSet rs = null;
  Connection conn = null;
  CallableStatement stmt = null;

  try {
    conn = DatabaseConfig.getConnection();
    stmt = conn.prepareCall(procedureCall);
    setParameters(stmt, params);

    boolean hasResults = stmt.execute();
    if (hasResults) {
      rs = stmt.getResultSet();
      resultList = getResults(rs, resultList);
    }

    return resultList;

  } catch (SQLException e) {
    throw new SQLException("Procedure call failed: " + e.getMessage());
  } finally {
    if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
    if (stmt != null) try { stmt.close(); } catch (SQLException ignore) {}
    if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
  }
}

}
