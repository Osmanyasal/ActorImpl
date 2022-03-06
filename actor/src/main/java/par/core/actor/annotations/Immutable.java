package par.core.actor.annotations;

/**
 * On class : Indicates that once the model is created cannot be changed
 * after.<br>
 * On method : Indicates that the method is final so it cannot be overritten by
 * it's child.
 * 
 * @author osman.yasal
 *
 */
@ThreadSafe
public @interface Immutable {
}
