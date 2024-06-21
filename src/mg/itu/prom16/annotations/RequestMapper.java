package mg.itu.prom16.annotations;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

public class RequestMapper {

    public static <T> T mapRequestToObject(HttpServletRequest request, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                RequestParam requestParam = field.getAnnotation(RequestParam.class);
                String paramName = (requestParam != null) ? requestParam.value() : field.getName();
                String paramValue = request.getParameter(paramName);

                if (paramValue != null) {
                    field.setAccessible(true);
                    field.set(instance, convertToFieldType(field.getType(), paramValue));
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping request to object", e);
        }
    }

    private static Object convertToFieldType(Class<?> fieldType, String value) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(value);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }
}

