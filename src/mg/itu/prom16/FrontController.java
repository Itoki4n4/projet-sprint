package mg.itu.prom16;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import annotation.*;
import controller.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    protected List<Class<?>> list_controller = new ArrayList<>();
    protected Map<String, Mapping> urlMappings = new HashMap<>();

    public void getControllerListSprint1(String package_name) throws ClassNotFoundException {
        String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");

        bin_path = getServletContext().getRealPath(bin_path);

        File b = new File(bin_path);

        list_controller.clear();
        
        for (File onefile : b.listFiles()) {
            if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(package_name + "." + onefile.getName().split(".class")[0]);
                if (clazz.isAnnotationPresent(annotation.AnnotationController.class)){
                    list_controller.add(clazz.getName());
                }
                
            }
        }
    }

    protected void getControllerList(String package_name) throws ClassNotFoundException {
        String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");
        bin_path = getServletContext().getRealPath(bin_path);
        File b = new File(bin_path);

        list_controller.clear();
        
        if (b.isDirectory()) {
            for (File onefile : b.listFiles()) {
                if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                    String className = package_name + "." + onefile.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(AnnotationController.class)) {
                        list_controller.add(clazz);

                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(GetAnnotation.class)) {
                                Mapping mapping = new Mapping(clazz.getName(), method.getName());
                                String key = method.getAnnotation(GetAnnotation.class).value();
                                urlMappings.put(key, mapping);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            getControllerList(getServletContext().getInitParameter("contextConfigLocation"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException("Failed to load controllers", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURI().substring(request.getContextPath().length());
        
        try (PrintWriter out = response.getWriter()) {
            Mapping mapping = urlMappings.get(url);
            out.println("<H1>Sprint S4</H1>");
            out.println("<H3>Sprint 0</H3>");
            out.println(request.getRequestURI());
            out.println("<H3>Sprint 1</H3>");

            try {
                getControllerListSprint1(getServletContext().getInitParameter("contextConfigLocation"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    
            try (PrintWriter out = response.getWriter()) {
                //out.println(request.getRequestURL().toString());
                // or out.println(request.getRequestURI());
    
                out.println("<ul>");
                for (String controller : list_controller) {
                    out.println("<li>"+controller+"</li>");
                }
                out.println("<ul>");
            }


            out.println();

            if (mapping != null) {
                out.println("URL: " + url + "</br>");
                out.println("Associated with: " + mapping);
            } else {
                out.println("No Get method associated with the URL: " + url);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
