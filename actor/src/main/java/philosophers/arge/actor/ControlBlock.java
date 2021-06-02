package philosophers.arge.actor;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ControlBlock {
	public enum Status {
		ACTIVE, PASSIVE
	}

	@Setter(value = AccessLevel.PRIVATE)
	private String id;

	private Status status;
	private Boolean isRoot;
	private ActorType type;

	public ControlBlock(ActorType type, Status status, boolean isRoot) {
		this.id = UUID.randomUUID().toString();
		this.status = status;
		this.type = type;
		this.isRoot = isRoot;
	}
}
