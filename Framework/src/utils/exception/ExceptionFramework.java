package mg.itu.utils.exception;

public class ExceptionFramework extends Exception{
    String code;

    public ExceptionFramework(String message){
        super(message);
    }

    public String getCode(){
        return code;
    }
}
