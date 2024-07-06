package mg.itu.utils.exception;

public class BadRequestException extends ExceptionFramework{
    public BadRequestException(String message){
        super(message);
        this.code = "400";

    }
}
