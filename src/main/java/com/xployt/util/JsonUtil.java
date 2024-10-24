package com.xployt.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonUtil {

    /**
     * Converts a Java object to a JSON string.
     *
     * @param object The Java object to convert.
     * @return JSON string representation of the object.
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "null"; // Handle null case
        }

        if (object instanceof String) {
            return "\"" + object + "\""; // Handle strings
        } else if (object instanceof Number || object instanceof Boolean) {
            return object.toString(); // Handle numbers and booleans
        } else if (object instanceof Collection<?>) {
            return toJsonArray((Collection<?>) object); // Handle collections
        } else if (object instanceof Map<?, ?>) {
            return toJsonMap((Map<?, ?>) object); // Handle maps
        } else if (object.getClass().isArray()) {
            return toJsonArray(object); // Handle arrays
        } else {
            return toJsonObject(object); // Handle custom objects
        }
    }

    /**
     * Converts a Java array to a JSON array string.
     *
     * @param array The Java array to convert.
     * @return JSON string representation of the array.
     */
    private static String toJsonArray(Object array) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("["); // Start of JSON array

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            jsonBuilder.append(toJson(element)); // Convert each array element to JSON
            if (i < length - 1) {
                jsonBuilder.append(","); // Add comma between elements
            }
        }

        jsonBuilder.append("]"); // End of JSON array
        return jsonBuilder.toString();
    }

    /**
     * Converts a Java collection (List, Set, etc.) to a JSON array string.
     *
     * @param collection The Java collection to convert.
     * @return JSON string representation of the collection.
     */
    private static String toJsonArray(Collection<?> collection) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("["); // Start of JSON array

        int size = collection.size();
        int index = 0;
        for (Object obj : collection) {
            jsonBuilder.append(toJson(obj)); // Convert each collection element to JSON
            if (index < size - 1) {
                jsonBuilder.append(","); // Add comma between elements
            }
            index++;
        }

        jsonBuilder.append("]"); // End of JSON array
        return jsonBuilder.toString();
    }

    /**
     * Converts a Java map to a JSON object string.
     *
     * @param map The Java map to convert.
     * @return JSON string representation of the map.
     */
    private static String toJsonMap(Map<?, ?> map) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{"); // Start of JSON object

        int size = map.size();
        int index = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            jsonBuilder.append("\"").append(entry.getKey()).append("\":");
            jsonBuilder.append(toJson(entry.getValue())); // Convert map value to JSON
            if (index < size - 1) {
                jsonBuilder.append(","); // Add comma between elements
            }
            index++;
        }

        jsonBuilder.append("}"); // End of JSON object
        return jsonBuilder.toString();
    }

    /**
     * Converts a custom Java object to a JSON object string.
     *
     * @param object The Java object to convert.
     * @return JSON string representation of the object.
     */
    private static String toJsonObject(Object object) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{"); // Start of JSON object

        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true); // Allow access to private fields

            try {
                jsonBuilder.append("\"").append(field.getName()).append("\":");
                Object value = field.get(object);
                jsonBuilder.append(toJson(value)); // Recursively convert field value to JSON

                if (i < fields.length - 1) {
                    jsonBuilder.append(","); // Add comma between fields
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        jsonBuilder.append("}"); // End of JSON object
        return jsonBuilder.toString();
    }
}
