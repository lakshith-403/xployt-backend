package com.xployt.testProps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class SetupUsers {

  public static int makeTestValidators(String name, String email, Connection conn) throws Exception {
    String sql = "INSERT INTO Users (email, passwordHash, name, role) VALUES (?, ?, ?, 'Validator')";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(3, name);
      stmt.setString(1, email);
      stmt.setString(2, "hash1");
      stmt.executeUpdate();

      String getUserId = "SELECT userId FROM Users WHERE email = ?";
      try (PreparedStatement stmt2 = conn.prepareStatement(getUserId)) {
        stmt2.setString(1, email);
        ResultSet rs = stmt2.executeQuery();
        rs.next();
        return rs.getInt("userId");
      }
    } catch (SQLException e) {
      System.out.println("Error making test validators: " + e.getMessage());
      throw e;
    }
  }

  public static int makeTestClient(String clientName, String email, Connection conn) throws Exception {
    String sql = "INSERT INTO Users (email, passwordHash, name, role) VALUES (?, ?, ?, 'Client')";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, email);
      stmt.setString(2, "hash1");
      stmt.setString(3, clientName);
      stmt.executeUpdate();

      String getClientId = "SELECT userId FROM Users WHERE email = ?";
      try (PreparedStatement stmt2 = conn.prepareStatement(getClientId)) {
        stmt2.setString(1, email);
        ResultSet rs = stmt2.executeQuery();
        rs.next();
        System.out.println("New test client id: " + rs.getInt("userId"));
        return rs.getInt("userId");
      }
    } catch (Exception e) {
      System.out.println("Error making test client: " + e.getMessage());
      throw e;
    }
  }

  public static int makeTestClientRandom(String clientName, String email, Connection conn) throws Exception {
    email = email + System.currentTimeMillis() + "@test.com";
    return makeTestClient(clientName, email, conn);
  }

  public static int makeProjectLead(String name, String email, Connection conn) throws Exception {
    String sql = "INSERT INTO Users (email, passwordHash, name, role) VALUES (?, ?, ?, 'ProjectLead')";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, email);
      stmt.setString(2, "hash1");
      stmt.setString(3, name);
      stmt.executeUpdate();

      String getProjectLeadId = "SELECT userId FROM Users WHERE email = ?";
      try (PreparedStatement stmt2 = conn.prepareStatement(getProjectLeadId)) {
        stmt2.setString(1, email);
        ResultSet rs = stmt2.executeQuery();
        rs.next();
        return rs.getInt("userId");
      }
    } catch (SQLException e) {
      System.out.println("Error making test project lead: " + e.getMessage());
      throw e;
    }
  }
}
