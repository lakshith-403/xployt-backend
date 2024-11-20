package com.xployt.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;

public class RequestBodyParsingFilter implements Filter {

  private static final Logger logger = CustomLogger.getLogger();

  private static final String APPLICATION_JSON = "application/json";
  private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization code, if needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    logger.info("RequestBodyParsingFilter: doFilter");
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);

      String contentType = wrappedRequest.getContentType();

      if (contentType != null) {
        if (contentType.contains(APPLICATION_JSON)) {
          parseJsonBody(wrappedRequest);
        } else if (contentType.contains(APPLICATION_FORM_URLENCODED)) {
          parseFormUrlEncodedBody(wrappedRequest);
        }
      }

      chain.doFilter(wrappedRequest, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private void parseJsonBody(HttpServletRequest request) throws IOException {
    logger.info("RequestBodyParsingFilter: parseJsonBody");
    logger.info("request: " + request);
    // logger.info("request.getReader(): " + request.getReader());
    // logger.info("request.getReader().lines(): " + request.getReader().lines());
    logger.info(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
    try (BufferedReader reader = request.getReader()) {
      String json = reader.lines().collect(Collectors.joining(System.lineSeparator()));
      logger.info("JSON String: " + json);

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> jsonMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
      });

      // Set attributes for each key-value pair
      for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
        request.setAttribute(entry.getKey(), entry.getValue().toString());
      }
    } catch (Exception e) {
      logger.severe("Error parsing JSON body: " + e.getMessage());
      throw new IOException("Failed to parse JSON body", e);
    }
  }

  private void parseFormUrlEncodedBody(HttpServletRequest request) throws IOException {
    logger.info("RequestBodyParsingFilter: parseFormUrlEncodedBody");
    Map<String, String[]> parameterMap = request.getParameterMap();

    // Set attributes for each key-value pair
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String[] values = entry.getValue();
      if (values != null && values.length > 0) {
        request.setAttribute(entry.getKey(), values[0]); // Assuming single value per key
      }
    }
  }

  @Override
  public void destroy() {
    // Cleanup code, if needed
    logger.info("RequestBodyParsingFilter: destroy");
  }
}