package philosophers.arge.actor;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is a tag of an object as like {@code ProcessControlBlock}. <br>
 * By using this tag we can get some information of the object.<br>
 * ex:<br>
 * <ul>
 * <li>id</li>
 * <li>status</li>
 * <li>isRoot</li>
 * <li>type</li>
 * </ul>
 * 
 * @author osmanyasal
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
	private ActorType type;

	private Boolean isRoot;

	private Status status;

	public ControlBlock(ActorType type, Status status, boolean isRoot) {
		this.id = UUID.randomUUID().toString();
		this.status = status;
		this.type = type;
		this.isRoot = isRoot;
	}
}
