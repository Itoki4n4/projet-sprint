package mg.itu.utils;

import java.util.Objects;

public class VerbAction {
    String methodName;
    String verb;
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }

    public VerbAction(){}
    public VerbAction(String methode, String verb){
        this.setMethodName(methode);
        this.setVerb(verb);
    }

    @Override 
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        VerbAction va = (VerbAction)o;
        return Objects.equals(this.getVerb(),va.getVerb());
        
    }

    @Override
    public int hashCode(){
        return Objects.hash(verb);
    }
}
