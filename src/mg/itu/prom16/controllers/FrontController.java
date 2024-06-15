package mg.itu.prom16.controllers;

import mg.itu.prom16.annotations.*;
import mg.itu.prom16.models.ModelView;  // Import correct du ModelView
import mg.itu.prom16.map.*;
import java.io.*;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.RequestDispatcher;  // Import correct pour RequestDispatcher
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    boolean checked = false;
    HashMap<String, Mapping> lien = new HashMap<>();
    String error="";

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        try{
            this.scan();
        }catch(Exception e){
            error=e.getMessage();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        response.setContentType("text/html");
        if(error != ""){
            out.println(error);
        }
        // else if(controllerPackage==null){
        //     out.println("le package n'existe pas");
        // }
        else if(!lien.containsKey(controllerSearched)) {
            out.println("<p>" + "Méthode non trouvée." + "</p>");
        } 
        else {
            try {
                Mapping mapping = lien.get(controllerSearched);
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method method = clazz.getMethod(mapping.getMethodeName());
                Object object = clazz.getDeclaredConstructor().newInstance();
                Object returnValue = method.invoke(object);
                if (returnValue instanceof String) {
                    out.println("Méthode trouvée dans " + (String) returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                    dispatcher.forward(request, response);
                } else {
                    out.println("Type de données non reconnu");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        out.close();
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

    public void scan()throws Exception{
        try {
    
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath + "\\" + controllerPackage.replace('.', '\\');
            File packageDirectory = new File(packagePath);
            if(!packageDirectory.exists() || !packageDirectory.isDirectory()){
                throw new Exception("Package n'existe pas");
            }
            
            else {
                File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
                if (classFiles != null) {
                    for (File classFile : classFiles) {
                        String className = controllerPackage + '.'
                                + classFile.getName().substring(0, classFile.getName().length() - 6);
                        try {
                            Class<?> classe = Class.forName(className); 
                            if (classe.isAnnotationPresent(Controller.class)) {
                                controller.add(classe.getSimpleName());

                                Method[] methodes = classe.getDeclaredMethods();

                                for (Method methode : methodes) {
                                    if (methode.isAnnotationPresent(Get.class)) {
                                        Mapping map = new Mapping(className, methode.getName());
                                        String valeur = methode.getAnnotation(Get.class).value();
                                        if(lien.containsKey(valeur)){
                                            throw new Exception("double url"+valeur);
                                        }else{
                                            lien.put(valeur, map);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                           throw e;
                        }

                    }
                }
                else{
                    throw new Exception("le package est vide");
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
