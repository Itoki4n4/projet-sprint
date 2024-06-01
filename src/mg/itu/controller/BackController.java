package mg.itu.controller; 
import mg.itu.annotation.*; 

@AnnotationController()
public class BackController {

    public BackController(){}

    @Get(value = "/")
    public String welcome(){
        return "Welcome to Tomcat 10.";
    }

    
}