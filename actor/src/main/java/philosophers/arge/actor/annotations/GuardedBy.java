package philosophers.arge.actor.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

/**
 * Indicates that what locking object is used.
 * 
 * @author osmanyasal
 *
 */
@Target({ FIELD, METHOD, CONSTRUCTOR })
public @interface GuardedBy {
	String value();
}
