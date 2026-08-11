package RawTomcat.dispatcher.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)          // Can only be placed on a CLASS
@Retention(RetentionPolicy.RUNTIME) // Available at runtime via reflection
public @interface MyController {
}
