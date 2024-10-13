package mg.itu.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
// import java.lang.Annotation;
import java.lang.annotation.Annotation;
// import java.text.Annotation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mg.itu.annotation.ControlleurAnnotation;
// import mg.itu.annotation.Get;
import mg.itu.annotation.Post;
import mg.itu.annotation.Url;
import mg.itu.annotation.type.*;


public class Scanner {
    public static ArrayList<Class> scanCurrentProjet(String packageName)throws Exception{
        ArrayList<Class>res = new ArrayList<>();
        String path;
        try{
                Thread currentThread = Thread.currentThread();
                ClassLoader classLoader = currentThread.getContextClassLoader();
                path = packageName.replace(".", "/");
                path = path.replace("%20"," ");
                java.net.URL ressource = classLoader.getResource(path);
                java.io.File directory = new java.io.File(ressource.getFile());
                for(java.io.File file : directory.listFiles()){
                    if(file.getName().endsWith(".class")){
                        String className = packageName + "."+ file.getName().substring(0,file.getName().length() - 6);
                        Class<?> cl = Class.forName(className);
                        ControlleurAnnotation annot = cl.getAnnotation(ControlleurAnnotation.class);
                        if(annot != null){
                            res.add(cl);
                        }
                    }
                }
        }catch(Exception e){
            String message = "Le nom de package "+packageName+" n'existe pas dans le projet";
            throw new Exception(message);
        }
        return res;
    }
    public static String buildMessage(String base,ArrayList message,String terminaison){
        String res = base + "\n";
        for(int i = 0; i<message.size(); i++){
            res += message.get(i) + terminaison + "\n";
        }
        return res;
    }

    //recherche de toutes les methodes
    public static Map<String,Mapping> scanMethod(String packageName) throws Exception{
        Map<String,Mapping> res = new HashMap<>();
        ArrayList<String>message = new ArrayList<>();
        try{
            ArrayList<Class>listClass = scanCurrentProjet(packageName);
            for(Object c : listClass){
                Class cl = (Class)c;
                Method[] listMethod = cl.getDeclaredMethods();
                for(Method me : listMethod){
                    Url url = me.getAnnotation(Url.class);
                    String className = cl.getName();
                    String methodName = me.getName();
                    String verb = "GET";
                    Post postAnnot = me.getDeclaredAnnotation(Post.class);
                    if(postAnnot != null){
                        verb = "POST";
                    }
                    if(url != null){
                        if(res.containsKey(url.url())){ //si l'url est deja repertorié
                            Mapping m = (Mapping)res.get(url.url());
                            if(!m.addVerbAction(methodName,verb)){ //verifie s'il n y a pas deja une methode de meme verbe associé à cet url
                                message.add(url.url());
                            }
                        }
                        else {
                            Mapping m = new Mapping();
                            m.setClassName(className);
                            m.addVerbAction(methodName,verb);
                            res.put(url.url(),m);
                        }
                    }
                }
            }
            if(message.size() > 0){
                String mes = buildMessage("Erreur au niveau des urls:", message," est un url d'une autre methode");
                Exception ex =  new Exception(mes);
                throw ex;
            }
        } catch(Exception e){
            throw new Exception(e.getMessage());
        }
        return res;
    }
    
    //recherche de toutes les view
    public static ArrayList<String> scanView(String container) throws Exception{
        ArrayList<String>res = new ArrayList<>();
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        String path = container.replace("%20"," ");
        java.net.URL ressource = classLoader.getResource(path); 
        ressource = new java.net.URL(ressource.toString().replace("%20"," "));
        java.io.File directory = new java.io.File(ressource.getFile());
        res.add(directory.getAbsolutePath());
        for(java.io.File file : directory.listFiles()){
            if(file.getName().endsWith(".jsp")){
                String fileName = file.getName();
                res.add(fileName);
            }
        }
        return res; 
    }

    //recuperation du types d'un parametre d'une methode
    public static Class<?> takeTypeField(Class<?> model, String fiel) {
        Class<?> res = null;
        try {
            Field[] fields = model.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(fiel)) {
                    res = field.getType();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Field takeField(Class<?> model, String name) throws Exception{
        Field target = null;
        try{
            target = model.getDeclaredField(name);
        } catch(Exception e){
            throw e;
        }
        return target;
    }

    //conversion des parametres
    public static Object convertParameterValue(Class<?> targetType, String parameterValue,String argName,Object value) throws Exception{
        String erreur = "Une valeur de type "+targetType.getSimpleName()+" est attendue pour l'entrée: "+argName+". Valeur trouvée : "+parameterValue;
        Object res = null;
        if (targetType == String.class) {
            res =  parameterValue;
        } else if (targetType == int.class || targetType == Integer.class) {
            try{
                res =  Integer.parseInt(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == long.class || targetType == Long.class) {
            try{
                res =  Long.parseLong(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == float.class || targetType == Float.class) {
            try{
                res =  Float.parseFloat(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == double.class || targetType == Double.class) {
            try{
                res =  Double.parseDouble(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            try{
                res =  Boolean.parseBoolean(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            } 
        }
        if(value == null){
            return res;
        }
        return value;
    }

    //conversion des parametres avec annotation
    public static Object convertParameterValueWithAnnot(Class<?> model, String parameterValue,String argName,Annotation ann,Object value,int nbAnnot) throws Exception{
        String targetType = "";
        String erreur = "Une valeur de type %s est attendue pour l'entrée: "+argName+". Valeur trouvée : %s";
        Field f = takeField(model, argName);
        Object res = null;
        if(ann.annotationType() == DateSQL.class){ //type date
            DateSQL annot = f.getDeclaredAnnotation(DateSQL.class);
            String format = annot.format();
            String type = annot.type();
            if(type.equals("timestamp")) res =  convertTimestamp(parameterValue, format);
            else res =  convertDate(parameterValue, format);

        } else if(ann.annotationType() == Numeric.class){ //type numeric
            try{
                res =  Integer.parseInt(parameterValue);
            }catch(Exception ie){
                try{
                    res =  Double.parseDouble(parameterValue);
                } catch(Exception de){
                    try{
                        res =  Float.parseFloat(parameterValue);
                    } catch(Exception fe){
                        try{
                            res =  Long.parseLong(parameterValue);
                        }catch(Exception e){
                            targetType = "Numeric";
                            throw new Exception(String.format(erreur,targetType,parameterValue));
                        }
                    }
                }
            } 
        } else if(ann.annotationType() == Text.class){ //type string
            Text t = f.getDeclaredAnnotation(Text.class);
            int longueurMax = t.longueurMax();
            int longueur = parameterValue.length();
            if(longueur>longueurMax){
                throw new Exception(t.errorMessage()+longueurMax+"Caractere autorisés");
            }
            res =  parameterValue;
        } else if(ann.annotationType() == Bool.class){ //type boolean
            try{
                res =  Boolean.parseBoolean(parameterValue);
            }catch(Exception e){
                throw new Exception(String.format(erreur, "Boolean",parameterValue));
            }
        } else if(ann.annotationType() == Length.class){
            int taille = parameterValue.length();
            if(taille > ((Length)ann).longueurMax()){
                throw new Exception(((Length)ann).errorMessage()+((Length)ann).longueurMax()+" autorisés");
            }
            if(nbAnnot == 1){
                Class<?> targetedType = takeTypeField(model, argName);
                res =  convertParameterValue(targetedType, parameterValue, argName,value);
            }
        }
        else{ //si pas d'annotation
            Class<?> targetedType = takeTypeField(model, argName);
            res =  convertParameterValue(targetedType, parameterValue, argName,value);
        }
        if(value == null){
            return res;
        }
        return value;
    }

    public static Date convertDate(String date,String format) throws Exception{
        Date d = null;
        try{
            SimpleDateFormat dF = new SimpleDateFormat(format);
            java.util.Date dT = dF.parse(date);
            d = new Date(dT.getTime());
        }catch(Exception e){
            throw e;
        }
        return d;
    }

    public static Timestamp convertTimestamp(String date,String format) throws Exception{
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(format);
        
        java.util.Date convertValue = dateTimeFormat.parse(date);
        Timestamp timestampValue = new Timestamp(convertValue.getTime());

        return timestampValue;
    }

    public static boolean isValidProfil(String profilActu,String[] profils){
        for(String p : profils){
            if(profilActu.equals(p)){
                return true;
            }
        }
        return false;
    }
}
