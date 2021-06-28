package philosophers.arge.actor;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target({ FIELD, METHOD, CONSTRUCTOR })
public @interface GuardedBy {
	String value();
}
