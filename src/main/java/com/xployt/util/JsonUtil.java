package com.xployt.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
//import java.lang.reflect.Field;
import java.util.Collection;

public class JsonUtil {

    /**
     * Converts a Java object to a JSON string.
     *
     * @param object The Java object to convert.
     * @return JSON string representation of the object.
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "{}"; // Return empty JSON object for null
        }

        if (object instanceof Collection<?>) {
            return toJsonArray((Collection<?>) object);
        } else {
            return toJsonObject(object);
        }
    }

    private static String toJsonObject(Object object) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true); // Allow access to private fields

            try {
                jsonBuilder.append("\"").append(field.getName()).append("\":");
                Object value = field.get(object);

                if (value instanceof String) {
                    jsonBuilder.append("\"").append(value).append("\"");
                } else {
                    jsonBuilder.append(value);
                }

                if (i < fields.length - 1) {
                    jsonBuilder.append(","); // Add comma if not the last element
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private static String toJsonArray(Collection<?> collection) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        int size = collection.size();
        int index = 0;
        for (Object obj : collection) {
            jsonBuilder.append(toJson(obj));
            if (index < size - 1) {
                jsonBuilder.append(","); // Add comma if not the last element
            }
            index++;
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    /**
     * Converts a JSON string to a Java object of the specified class.
     *
     * @param json  The JSON string to convert.
     * @param clazz The class of the object to convert to.
     * @param <T>   The type of the object.
     * @return The converted Java object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            // Remove braces and split by comma
            json = json.trim().replaceAll("^\\{|\\}$", "");
            String[] keyValuePairs = json.split(",");

            Map<String, String> map = new HashMap<>();
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split(":", 2);
                String key = keyValue[0].trim().replaceAll("^\"|\"$", ""); // Remove quotes
                String value = keyValue[1].trim().replaceAll("^\"|\"$", ""); // Remove quotes
                map.put(key, value);
            }

            T instance = clazz.getDeclaredConstructor().newInstance(); // Create new instance
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Field field = clazz.getDeclaredField(entry.getKey());
                field.setAccessible(true); // Allow access to private fields

                // Attempt to set the field value based on its type
                if (field.getType() == String.class) {
                    field.set(instance, entry.getValue());
                } else if (field.getType() == int.class) {
                    field.set(instance, Integer.parseInt(entry.getValue()));
                }
                // Add other types as needed (e.g., boolean, double)
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle error cases
        }
    }
}

