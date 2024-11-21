package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;

import com.xployt.util.ContextManager;
import com.xployt.model.Client;
import com.xployt.util.CustomLogger;
import java.util.logging.Logger;

public class ClientDAO {

  private static final Logger logger = CustomLogger.getLogger();

  public void createClient(Client client) {

    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");

    String sql = "INSERT INTO clients (client_id, client_name, email, username) VALUES (?, ?, ?, ?)";

    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
      logger.info("sql: " + sql);
      preparedStatement.setInt(1, client.getClientId());
      preparedStatement.setString(2, client.getClientName());
      preparedStatement.setString(3, client.getEmail());
      preparedStatement.setString(4, client.getUsername());

      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      // Handle exceptions appropriately
    }
  }

  public Client getClientById(int clientId) {
    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");

    String sql = "SELECT * FROM clients WHERE client_id = ?";
    Client client = null;

    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

      preparedStatement.setInt(1, clientId);
      ResultSet resultSet = preparedStatement.executeQuery();

      if (resultSet.next()) {
        client = new Client();
        client.setClientId(resultSet.getInt("client_id"));
        client.setClientName(resultSet.getString("client_name"));
        client.setEmail(resultSet.getString("email"));
        client.setUsername(resultSet.getString("username"));
      }

    } catch (SQLException e) {
      e.printStackTrace();
      // Handle exceptions appropriately
    }

    return client;
  }

  // Additional methods for update and delete can be added similarly
}