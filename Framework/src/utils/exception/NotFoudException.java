package mg.itu.utils.exception;

public class NotFoudException extends ExceptionFramework{

    public NotFoudException(String message){
        super(message);
        this.code = "404";
    }
}
