package mg.itu.prom16;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.net.URLDecoder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mg.itu.annotation.AnnotationController;
import mg.itu.annotation.Get;

public class FrontController extends HttpServlet {
    Map<String, Mapping> urlMappings = new HashMap<>();
    boolean checked = false;
    ArrayList<Class<?>> list;

    public void init() throws ServletException {
        try {
            scanner();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Failed to load controllers", e);
        }
    }

    private void scanner(){
        if (!checked) {
            String pack = getServletConfig().getInitParameter("ControllerPackage");
            try {
                list = getClassesInSpecificPackage(pack);
                getListController(pack);
                checked = true;
            } catch (Exception e) {
                e.printStackTrace();
                list = new ArrayList<>();
            }
        }
    }

    private void getListController(String package_name) throws ClassNotFoundException {
        String path = "WEB-INF/classes/" + package_name.replace(".", "/");
        path = getServletContext().getRealPath(path);
        File file = new File(path);

        list.clear();
        
        if (file.isDirectory()) {
            for (File uniquefile : file.listFiles()) {
                if (uniquefile.isFile() && uniquefile.getName().endsWith(".class")) {
                    String className = package_name + "." + uniquefile.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(AnnotationController.class)) {
                        list.add(clazz);

                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(Get.class)) {
                                Mapping mapping = new Mapping(clazz.getName(), method.getName());
                                String key = method.getAnnotation(Get.class).value();
                                urlMappings.put(key, mapping);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private ArrayList<Class<?>> getClassesInSpecificPackage(String packageName) throws IOException, ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        System.out.println(path);

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
            }
        }
        return classes;
    }

    private void getValueInMethod(Mapping map, PrintWriter out){
        try {
            Class<?> clazz = Class.forName(map.getClassName());
            Object instance = clazz.getConstructor().newInstance();
            Method method = clazz.getMethod(map.getMethodName());
            Object result = method.invoke(instance);
            out.println("The value returned by the method <b>" + map.getMethodName() + "</b> is: <b>" + result.toString()+"<b>");
        } catch (Exception e) {
            out.println("Error: "+e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURI().substring(request.getContextPath().length());
        try (PrintWriter out = response.getWriter()) {
            Mapping mapping = urlMappings.get(url);
            if (mapping != null) {
                getValueInMethod(mapping, out);
            } else {
                out.println("No Get method associated with the URL: <b>" + url+"</b>");
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("Erreur: " + ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("Erreur: " + ex.getMessage());
        }
    }
}
