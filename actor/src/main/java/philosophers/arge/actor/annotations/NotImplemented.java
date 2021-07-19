package philosophers.arge.actor.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 
 * @author osmanyasal
 *
 */

@Target({ FIELD, METHOD, CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
public @interface NotImplemented {

}
