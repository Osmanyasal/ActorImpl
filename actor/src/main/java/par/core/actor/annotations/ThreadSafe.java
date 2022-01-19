package par.core.actor.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This is a marker interface that emphasise that the method or class which uses
 * this interface is thread safe. <br>
 * Which means we can call the method or class in different threads
 * symentanously.
 * 
 * @author osmanyasal
 *
 */

@Target({ FIELD, METHOD, CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
public @interface ThreadSafe {

}
