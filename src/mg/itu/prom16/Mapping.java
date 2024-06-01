package mg.itu.prom16;

public class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+":{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}

