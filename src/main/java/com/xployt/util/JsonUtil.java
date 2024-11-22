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

        int size = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) { // Ignore null values
                if (size > 0) {
                    jsonBuilder.append(","); // Add comma between elements
                }
                jsonBuilder.append("\"").append(entry.getKey()).append("\":");
                jsonBuilder.append(toJson(entry.getValue())); // Convert map value to JSON
                size++;
            }
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
        int size = 0;
        for (Field field : fields) {
            field.setAccessible(true); // Allow access to private fields

            try {
                Object value = field.get(object);
                if (value != null) { // Ignore null values
                    if (size > 0) {
                        jsonBuilder.append(","); // Add comma between fields
                    }
                    jsonBuilder.append("\"").append(field.getName()).append("\":");
                    jsonBuilder.append(toJson(value)); // Recursively convert field value to JSON
                    size++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        jsonBuilder.append("}"); // End of JSON object
        return jsonBuilder.toString();
    }

    /**
     * Converts a JSON string to a Java object of the specified class.
     *
     * @param json The JSON string to convert
     * @param clazz The class type to convert to
     * @return An instance of the specified class
     * @throws Exception if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            
            // Remove curly braces and split by commas
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new Exception("Invalid JSON format");
            }
            
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length != 2) continue;
                
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                
                try {
                    Field field = clazz.getDeclaredField(key);
                    field.setAccessible(true);
                    
                    // Convert value to appropriate type
                    if (field.getType() == String.class) {
                        field.set(instance, value);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(instance, Integer.parseInt(value));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(instance, Boolean.parseBoolean(value));
                    }
                    // Add more type conversions as needed
                    
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Skip fields that don't exist or can't be accessed
                    continue;
                }
            }
            
            return instance;
        } catch (Exception e) {
            throw new Exception("Error parsing JSON: " + e.getMessage());
        }
    }
}
