package mg.itu.sprint;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import jakarta.servlet.annotation.MultipartConfig;




import mg.itu.utils.Scanner;
import mg.itu.utils.Mapping;
import mg.itu.utils.ModelView;
import mg.itu.utils.MySession;
import mg.itu.utils.FileMap;

import mg.itu.utils.exception.*;

import mg.itu.annotation.Param;
import mg.itu.annotation.RestAPI;
import mg.itu.annotation.IfError;
import mg.itu.annotation.Authentification;
import mg.itu.annotation.type.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@MultipartConfig
public class FrontController extends HttpServlet {
    Map<String, Mapping> urlMap;
    ArrayList<String> urlView;
    String authSessionName;
    String message;
    boolean visited;
    static String uriOrg = "";

    public void init() throws ServletException {
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        authSessionName = this.getInitParameter("authSessionName");
        try {
            this.urlMap = Scanner.scanMethod(controllPackage);
            this.urlView = Scanner.scanView("../..");
        } catch (Exception e) {
            message = "Erreur au niveau du build du projet. Veuillez consulter votre terminal";
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse rep)
            throws ServletException, IOException {
        String prevu = req.getMethod();
        PrintWriter out = rep.getWriter();
        Map<String, String> paramMap = new HashMap<>();
        boolean ignoreConstraint = false;
        HttpSession sess = req.getSession();
        if(req.getAttribute("ignore") != null){
            ignoreConstraint = true;
            prevu = "GET";
            req.removeAttribute("ignore");
        }
        if (message != null) {
            displayError(message, "400", rep);
        } else {
            String url = req.getRequestURL().toString();
            System.out.println("url passage:"+url);
            String[] urP = url.split("\\?"); // separation du lien et des parametres dans le liens
            String[] urlParts = urP[0].split("/"); // recuperation des differentes parties du lien
            int i = 1;
            String urlTarget = "/" + urlParts[urlParts.length - 1];
            boolean ver = false;
            try {
                this.methodExist(url);
                while (i < urlParts.length) {
                    if (this.urlMap.containsKey(urlTarget)) {
                        Mapping mapping = this.urlMap.get(urlTarget);
                        if(!mapping.contains(prevu) && !ignoreConstraint){//verifie si post ou get 
                            displayError("Erreur de requête : Une requête de type "+prevu+" est attendue", "404", rep);
                            return;
                        }
                        try {
                            Object obj = executeMethode(mapping, req,prevu,rep,authSessionName);
                            if (obj instanceof String) {
                                if(isJson((String)obj)){ //si l'objet est au format json
                                    rep.setContentType("application/json");
                                    rep.setCharacterEncoding("UTF-8");
                                }
                                out.print((String) obj);
                            } else if (obj instanceof ModelView) {
                                ModelView modelV = (ModelView) obj;
                                Map<String, Object> map = modelV.getData();
                                try {
                                    this.viewExist(modelV.getViewUrl());
                                    RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
                                    System.out.println(modelV.getViewUrl());
                                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                                        String dataName = (String) entry.getKey();
                                        Object data = entry.getValue();
                                        req.setAttribute(dataName, data);
                                    }
                                    dispat.forward(req, rep);
                                } catch (ExceptionFramework e) {
                                    // out.print(e.getMessage());
                                    displayError(e.getMessage(), e.getCode(), rep);
                                    return;
                                }
                            }
                        } catch (ExceptionFramework e) {
                            // e.printStackTrace();
                            // RequestDispatcher dispat = req.getRequestDispatcher("erreur.jsp");
                            // req.setAttribute("erreur",e.getMessage());
                            // dispat.forward(req,rep);
                            // // out.println(e.getMessage());
                            displayError(e.getMessage(), e.getCode(), rep);
                            return;
                        }
                        ver = true;
                        break;
                    } else {
                        urlTarget = "/" + urlParts[urlParts.length - (i + 1)] + urlTarget;
                    }
                    i++;
                }
            } catch (Exception e) {
                displayError(e.getMessage(), "400", rep);
                return;
            }
        }
    }

    public static Object executeMethode(Mapping target,HttpServletRequest req,String prevu,HttpServletResponse rep,String sessionName) throws Exception{
        HttpSession session = req.getSession();

        //recuperation des noms des parametres
        Enumeration<String> paramNames = req.getParameterNames();
        ArrayList<String> parametersNames = new ArrayList<>();
        while(paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            parametersNames.add(paramName); //stockage des noms dans une liste
        } 

        
        String className = target.getClassName(); //nom de la classe contenu dans le mapping
        String methodeName = target.getMethodName(prevu); //nom de la methode a invoquée
        System.out.println("prevu:"+methodeName);
        Class<?>cl = Class.forName(className);//Recuperation de la classe qui va invoquer la methode

        //verification des autorisations pour la classe
        Authentification authM = cl.getAnnotation(Authentification.class);
        System.out.println(sessionName);
        if(authM != null){ //verification des authentification
            String[] authorizedRoles = authM.role();
            String role = (String)session.getAttribute(sessionName);
            if(!Scanner.isValidProfil(role,authorizedRoles)){
                throw new AuthException("Insufficient privileges");
            }
        }

        Method[] mes = cl.getDeclaredMethods();//Recuperation de la liste des methodes
        boolean hasAnnot = true;
        String uriError = "";
        //recherche de la methode correspondante
        for(Method m : mes){
            if(methodeName.compareTo(m.getName()) == 0){ //si le nom correspond
                Method me = m;
                Authentification auth = me.getAnnotation(Authentification.class);
                if(auth != null){ //verification des authentification
                    String[] authorizedRoles = auth.role();
                    String role = (String)session.getAttribute(sessionName);
                    if(!Scanner.isValidProfil(role,authorizedRoles)){
                        throw new AuthException("Insufficient privileges");
                    }
                }
                Parameter[] parms = me.getParameters(); //recuperation des parametres de la methode
                int countParam = parms.length; //nombre d'argument de la methode
                Object instance = cl.getDeclaredConstructor().newInstance(); //instanciation de l'objet qui va executer la methode
                Object obj = null;
                boolean isRestAPI = false; //dit qu'il n'y a pas d'annotation restAPI
                if(me.getAnnotation(RestAPI.class) != null){ //si il y a l'annotation restAPI
                    isRestAPI = true;
                }
                if(countParam >= 1){ //si la methode possede des parametres
                    ArrayList<Object> paramO = new ArrayList<>();
                    ArrayList<String>passage = new ArrayList<>(); //pour verifier si on est pas deja passer par le parametre
                    boolean hasError = false;
                    for(Parameter p : parms){
                        Class<?> paramType = p.getType(); //recuperation du type du parametre
                        String typeName = paramType.getSimpleName(); //type du parametre
                        String annot;
                        if(paramType.getSimpleName().compareTo("MySession") == 0){
                            MySession sess = (MySession)(paramType.getDeclaredConstructor(HttpSession.class).newInstance(session));
                            paramO.add(sess);
                        } else if(paramType.getSimpleName().compareTo("FileMap") == 0){
                            FileMap files = (FileMap)fileHandling(req, p)[0];
                            paramO.add(files);
                        }
                        else{
                            if(p.getAnnotation(Param.class) != null){ //si le parametre possede une annotation
                                annot = p.getAnnotation(Param.class).name(); //on prend la valeur de l'annnotation
                            }else{
                                throw new Exception("ETU002474 Erreur: pas d'annotation");
                                // annot = p.getName(); // on prend le nom du parametre
                            } 

                            Map<String,ArrayList<String>> errorMessage = new HashMap<>();
                            for(String par : parametersNames){
                                String[] paramParts = par.split("_");//separation du parametre pour savoir si on a besoin d'un objet
                                String argName = "";
                                if(paramParts.length > 1){ //si c'est le cas
                                    String objName = paramParts[0]; //nom de la classe object
                                    argName = paramParts[1]; //nom du parametre
                                    if(annot.compareTo(objName) == 0){ //validite du parametre
                                        if(!passage.contains(annot)){
                                            Object instanceParam = paramType.getDeclaredConstructor().newInstance(); //instanciation de l'objet
                                            paramO.add(instanceParam);//ajout de l'objet à la liste des parametre
                                            passage.add(annot);//marquer comme passer
                                        }
                                        Object value = null;
                                        try{
                                            Field f = Scanner.takeField(paramType,argName);
                                            req.setAttribute(par,req.getParameter(par)); //stockage de la valeur en cas de redirection
                                            Annotation[] mAnnots = f.getAnnotations();
                                            for(Annotation ann: mAnnots){
                                                try{
                                                    value = Scanner.convertParameterValueWithAnnot(paramType, req.getParameter(par), argName,ann,value,mAnnots.length); 
                                                }catch(Exception e){
                                                    //stockage des erreur
                                                    hasError = true;
                                                    if(errorMessage.get(par) == null){
                                                        errorMessage.put(par,new ArrayList<>());
                                                    }
                                                    errorMessage.get(par).add(e.getMessage());
                                                }
                                                
                                            }
                                            //conversion de la valeur avec des annotations
                                            Object inst = paramO.get(paramO.size() - 1); //prise du dernier parametre
                                            String methName = "set"+argName.substring(0,1).toUpperCase() + argName.substring(1); //nom du setteur correspondant
                                            Method set;
                                            if (value instanceof Integer) {
                                                set = paramType.getMethod(methName, int.class);    
                                            } else{
                                                set = paramType.getMethod(methName, value.getClass());   
                                            }
                                            set.invoke(inst, value);
                                            
                                        } catch(Exception e){

                                        }

                                        
                                    }
                                } else{
                                    argName = paramParts[0];
                                    if(argName.compareTo(annot) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                        Object value = null;
                                        value = Scanner.convertParameterValue(paramType, req.getParameter(argName),argName,value);//Conversion du parametre
                                        paramO.add(value); //ajout du parametre
                                        break;
                                    }
                                }
                            }

                            if(hasError){

                                if(me.getAnnotation(IfError.class) != null){
                                    uriError = me.getAnnotation(IfError.class).url(); 
                                    System.out.println("direction: "+uriError);         
                                } else{
                                    throw new NotFoudException("Impossible de trouver la page");
                                }
                                RequestDispatcher dispat = req.getRequestDispatcher(uriError);
                                req.setAttribute("error",errorMessage);
                                HttpServletRequest wrap = new HttpServletRequestWrapper(req){
                                    @Override
                                    public String getMethod(){
                                        return "GET";
                                    }
                                };
                                dispat.forward(wrap,rep);
                            } else{
                                uriOrg = "";
                            }
                        }
                    }
                    Object[] p = paramO.toArray(); //conversion de la liste de valeur en tableau d'objet
                    try{
                        obj = me.invoke(instance,p);
                    }catch(Exception e){
                        throw new BadRequestException(e.getMessage());
                    }
                     //invocation de la methode avec parametre
                }
                else{ //sinon invocation de la methode sans parametre
                    try{
                        obj= me.invoke(instance);
                    }catch(Exception e){
                        throw new BadRequestException(e.getMessage());
                    }
                    
                }
                if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelView") != 0){ //Exception si ce n'est ni un String ni un modelView
                    throw new BadRequestException("La methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelView, String");
                }
                if(isRestAPI){//si c'est un restAPI
                    if(obj.getClass().getSimpleName().compareTo("ModelView") == 0){
                        
                        obj = ((ModelView)obj).getData();
                    }
                    Gson gson = new Gson();
                    obj = gson.toJson(obj);
                }
                return obj;
            }
        }
        return null;
    }

    
    //si la vue exist
    public void viewExist(String viewUrl) throws Exception {
        ArrayList<String> listView = this.urlView;
        if (!listView.contains(viewUrl)) {
            throw new NotFoudException("La page " + viewUrl + " n'existe pas!");
        }
    }
    //si la methodExist
    public void methodExist(String urlMethod) throws Exception {
        Map<String, Mapping> urlList = this.urlMap;
        String[] urlParts = urlMethod.split("/");
        String urlTarget = "/" + urlParts[urlParts.length - 1];
        int i = 1;
        while (i < urlParts.length) {
            if (this.urlMap.containsKey(urlTarget)) {
                return;
            }
            urlTarget = "/" + urlParts[urlParts.length - (i + 1)] + urlTarget;
            i++;
        }
        throw new NotFoudException("L'url " + urlMethod + " n'est associé à aucune méthode du projet");
    }

    //si une chaine de caractere est au format json
    public static boolean isJson(String value){ 
        try{
            JsonElement jsonElement = JsonParser.parseString(value);
            return jsonElement.isJsonObject() || jsonElement.isJsonArray();
        } catch(JsonSyntaxException e){
            return false;
        }
    }

    //display de l'erreur (sprint 11)
    protected static void displayError(String error,String code,HttpServletResponse rep) throws IOException,ServletException{
        rep.setContentType("text/html");
        PrintWriter out = rep.getWriter();

        //page 
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Erreur</title>");

        // CSS intégré dans la balise <style>
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f0f0f0; }");
        out.println("h1 { color: red; }");
        out.println("p { font-size: 16px; }");
        out.println("ul { list-style-type: none; }");
        out.println("li { background-color: #e0e0e0; margin: 5px 0; padding: 10px; border-radius: 5px; }");
        out.println("</style>");
        //fin style 

        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Vous avez rencontre un probleme</h1>");
        out.println("<p>Code d'erreur : <strong>"+code+"</strong></p>");
        out.println("<p>Message: <strong>"+error+"</strong></p>");
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");
    }

    //recuperation du nom de fichier
    static String getFileName(Part part){
        String disposition = part.getHeader("content-disposition");
        for(String content: disposition.split(";")){
            if(content.trim().startsWith("filename")){
                return content.substring(content.indexOf('=') + 1).trim().replace("\"","");
            }
        }
        return null;
    }
    //recuperation des byte d'un fichier 
    static byte[] getByte(Part part) throws Exception{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream fileContent = null;
        try{
            fileContent = part.getInputStream();
            byte[] data = new byte[1024];
            int reader;
            while((reader = fileContent.read(data,0,data.length)) != -1){
                buffer.write(data,0,reader);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            fileContent.close();
        }
        return buffer.toByteArray();
    }

    //Recuperation des infos
    static FileMap getFileInfo(HttpServletRequest req,Parameter p) throws Exception{
        if(p.getType().equals(FileMap.class)){
            Param annotation = p.getAnnotation(Param.class);
            Part part = req.getPart(annotation.name());
            String fileName = getFileName(part);
            byte[] b = getByte(part);
            return new FileMap(fileName, b);
        }
        return null;
        
    }

    //gestion des fichiers sprint12
    static Object[] fileHandling(HttpServletRequest req,Parameter param) throws Exception{
        Object[] files = null;
        try{
            FileMap fileInfo = getFileInfo(req, param);

            if(fileInfo != null){
                files = new Object[1];
                files[0] = fileInfo;
            }
            
        }catch(Exception e){
            throw e;
        }
        return files;
    }
}
