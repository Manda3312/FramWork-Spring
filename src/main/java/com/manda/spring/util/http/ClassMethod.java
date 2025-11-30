package com.manda.spring.util.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.manda.spring.annotation.controller.PathVariable;
import com.manda.spring.annotation.controller.RequestParameter;
import com.manda.spring.servlet.route.Route;

import jakarta.servlet.http.HttpServletRequest;

public class ClassMethod {
    private Class<?> c;
    private Method m;

    public ClassMethod(Class<?> c, Method m) {
        this.c = c;
        this.m = m;
        m.setAccessible(true);
    }

    public Object invokeMethod() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> controllerConstructor = c.getDeclaredConstructor();
        Object controller = controllerConstructor.newInstance();
        return m.invoke(controller);
    }

    public Object invokeMethod(Route route, HttpServletRequest req) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> controllerConstructor = c.getDeclaredConstructor();
        Object controller = controllerConstructor.newInstance();

        Parameter[] parameters = m.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            String paramName = getParameterName(parameter);
            Object paramValue = getParameterValue(paramName, parameter, req, route);

            args[i] = convertValue(paramValue, parameter.getType(), paramName);
        }

        return m.invoke(controller, args);
    }

    private String getParameterName(Parameter parameter) {
        RequestParameter rp = parameter.getAnnotation(RequestParameter.class);
        if (rp != null) {
            return rp.value();
        }

        PathVariable pv = parameter.getAnnotation(PathVariable.class);
        if (pv != null) {
            return pv.value();
        }

        return parameter.getName();
    }

    private Object getParameterValue(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        String value = req.getParameter(paramName);
        if (value != null) {
            return value;
        }

        PathVariable pv = parameter.getAnnotation(PathVariable.class);
        if (pv != null) {
            String uri = Route.getLocalURIPath(req);
            Map<String, String> pathVars = route.getPathVariableValues(uri);
            return pathVars.get(paramName);
        }

        return null;
    }

    private Object convertValue(Object value, Class<?> targetType, String paramName) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Required primitive parameter '" + paramName + "' is missing");
            } else {
                return null;
            }
        }

        String strValue = value.toString();
        try {
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(strValue);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(strValue);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(strValue);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(strValue);
            } else if (targetType == String.class) {
                return strValue;
            } else {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for parameter '" + paramName + "': " + strValue, e);
        }
    }

    public Class<?> getC() { return c; }
    public void setC(Class<?> c) { this.c = c; }
    public Method getM() { return m; }
    public void setM(Method m) { this.m = m; }
}
