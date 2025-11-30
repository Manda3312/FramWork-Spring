package com.manda.spring.servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.manda.spring.annotation.ControllerAnnotation;
import com.manda.spring.util.http.ClassMethod;
import com.manda.spring.util.scan.ClassScanner;
import com.manda.spring.util.scan.MethodScanner;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class mandaServletContextListener implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute("urlCmMap", getUrlMethodMap());
    }

    /**
     * Gets all the url - CM during init()
     * and inserts it in a map
     */

    private Map<String, ClassMethod> getUrlMethodMap() {
        Map<String, ClassMethod> map = new HashMap<>();

        Set<Class<?>> classes = ClassScanner.getInstance().getClassesAnnotatedWith(ControllerAnnotation.class, "com.manda");

        System.out.println("Valid backend URLs: ");
        for (Class<?> c : classes) {
            Map<String, Method> urlMappingPathMap = MethodScanner.getInstance().getAllUrlMappingPathValues(c);

            for (String url : urlMappingPathMap.keySet()) {
                Method m = urlMappingPathMap.get(url);
                ClassMethod cm = new ClassMethod(c, m);
                map.put(url, cm);

                System.out.println("\t" + url);
            }
        }
        return map;
    }
}
