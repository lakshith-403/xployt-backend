package com.xployt.util;

import jakarta.servlet.ServletContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ContextManager {
  private static final Map<String, ServletContext> contextMap = new ConcurrentHashMap<>();

  // Method to register a context
  public static void registerContext(String contextName, ServletContext context) {
    contextMap.put(contextName, context);
  }

  // Method to retrieve a context
  public static ServletContext getContext(String contextName) {
    return contextMap.get(contextName);
  }

  // Method to remove a context
  public static void removeContext(String contextName) {
    contextMap.remove(contextName);
  }
}
