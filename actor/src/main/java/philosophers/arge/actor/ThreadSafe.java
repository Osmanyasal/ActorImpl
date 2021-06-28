package philosophers.arge.actor;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

/**
 * This is a marker interface that emphasise that the method or class which uses
 * this interface is thread safe. which means we can call the method or class in
 * different threads symentanously.
 * 
 * @author osmanyasal
 *
 */

@Target({ FIELD, METHOD, CONSTRUCTOR })
public @interface ThreadSafe {

}
