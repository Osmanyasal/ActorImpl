package par.core.actor.base;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is a tag of a Node just like {@code ProcessControlBlock}. <br>
 * By using this tag we can get some information of the Node.<br>
 * contains:<br>
 * <ul>
 * <li>id</li>
 * <li>status</li>
 * <li>isRoot</li>
 * <li>type</li>
 * </ul>
 * 
 * @author osman.yasal
 *
 */
@Data
@Accessors(chain = true)
public class ControlBlock {
	public enum Status {
		ACTIVE, PASSIVE
	}
	@Setter(value = AccessLevel.PRIVATE)
	private String id;

	@Setter(value = AccessLevel.PRIVATE)
	private Type type;

	private boolean isRoot;
	private Status status;

	public ControlBlock(Type type, Status status, boolean isRoot) {
		this.id = UUID.randomUUID().toString();
		this.status = status;
		this.type = type;
		this.isRoot = isRoot;
	}
}
