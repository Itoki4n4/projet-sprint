package mg.itu.annotation.type;

import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Text {
    int longueurMax() default 255;
    String errorMessage() default "Le texte depasse la limite autorisée : ";
}
