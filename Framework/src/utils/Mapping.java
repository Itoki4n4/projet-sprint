package mg.itu.utils;

// import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Mapping {
    String className;
    Set<VerbAction> verbAction;
    
    public Mapping(String cl){
        this.setClassName(cl);
    }
    public Mapping(){}

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodName(String verb){
        for(VerbAction va : this.verbAction){
            if(va.getVerb().equals(verb)){
                return va.getMethodName();
            }
        }
        return null;
    }
    // public String getMethodName() {
    //     return methodName;
    // }
    // public void setMethodName(String methodName) {
    //     this.methodName = methodName;
    // }
    // public void setVerb(String verb){
    //     this.verb = verb;
    // }
    // public String getVerb(){
    //     return verb;
    // }
    public Set<VerbAction> getVerbAction() {
        return verbAction;
    }
    public void setVerbAction(Set<VerbAction> verbAction) {
        this.verbAction = verbAction;
    }
    public boolean addVerbAction(String methode,String verb){
        if(this.verbAction == null){
            this.verbAction = new HashSet<>();
        }

        return this.verbAction.add(new VerbAction(methode,verb));
    }
    public boolean contains(String verb){
        for(VerbAction ver : this.verbAction){
            if(ver.getVerb().equalsIgnoreCase(verb)){
                return true;
            }
        }
        return false;
    }
    
}