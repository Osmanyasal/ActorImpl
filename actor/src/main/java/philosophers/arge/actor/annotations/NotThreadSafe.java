package philosophers.arge.actor.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.concurrent.locks.Lock;

/**
 * This is a marker interface that emphasise that the method or class which uses
 * this interface is NOT thread safe. which means we CAN'T SAFELY call the
 * method or class in different threads symentanously.
 * 
 * in order to call related method or class from different threads, we MUST
 * establish an accessing protocol in order to avoid complications
 * 
 * @see Lock
 * @author osmanyasal
 *
 */
@Target({ FIELD, METHOD, CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
public @interface NotThreadSafe {

}
