package mg.itu.utils.exception;

public class ProcessException extends Exception{
    String name;
    String code;

    public ProcessException(String n,String c,String e){
        super(e);
        this.name = n;
        this.code = c;
    }

    public String getCode(){
        return this.code;
    }
}
