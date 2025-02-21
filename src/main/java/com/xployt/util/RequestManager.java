package com.xployt.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestManager {

  /**
   * Filters a list of maps to only include specified fields.
   *
   * @param objects - The list of maps to filter.
   * @param fields  - The list of field names to include in the resulting maps.
   * @return A list of maps containing only the specified fields.
   */
  public static List<Map<String, Object>> filterObjectsByFields(List<Map<String, Object>> objects,
      List<String> fields) {
    return objects.stream()
        .map(obj -> fields.stream()
            .filter(obj::containsKey)
            .collect(Collectors.toMap(field -> field, obj::get)))
        .collect(Collectors.toList());
  }

  /**
   * Filters a list of maps to exclude specified fields.
   *
   * @param objects - The list of maps to filter.
   * @param fields  - The list of field names to exclude from the resulting maps.
   * @return A list of maps excluding the specified fields.
   */
  public static List<Map<String, Object>> excludeFieldsFromObjects(List<Map<String, Object>> objects,
      List<String> fields) {
    return objects.stream()
        .map(obj -> obj.entrySet().stream()
            .filter(entry -> !fields.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .collect(Collectors.toList());
  }
}
