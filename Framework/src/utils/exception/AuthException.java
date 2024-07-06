package mg.itu.utils.exception;

public class AuthException extends ExceptionFramework{
    public AuthException(String message){
        super(message);
        this.code = "401";

    }
}
