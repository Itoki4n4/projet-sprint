package mg.itu.prom16;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.URLDecoder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mg.itu.annotation.AnnotationController;

public class FrontController extends HttpServlet {

    boolean checked = false;
    String path;
    ArrayList<Class<?>> list;

    public void init() throws ServletException {
        super.init();
        scanner();
    }
    
    private void scanner(){

        if (!checked) {
            String pack = getServletConfig().getInitParameter("ControllerPackage");
            try {
                System.out.println(getClassesInSpecificPackage(pack));
                list = getClassesInSpecificPackage(pack);
                checked = true;
            } catch (Exception e) {
                e.printStackTrace();
                list = new ArrayList<>();
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) { 
            out.print("Voici les listes des Controllers:\n");
            if (list.isEmpty()) {
                out.print("La liste est vide.");
            }
            out.println("<ul>");
            for (int i = 0; i < list.size(); i++) {
                out.print("<li>"+list.get(i).getSimpleName()+"</li>");
            }
            out.println("</ul>");
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            out.println("Erreur: " + e.getMessage());
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
