package philosophers.arge.actor;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ControlBlock {
	public enum Status {
		ACTIVE, PASSIVE
	}

	private String id;
	private Status status;
	private Boolean isRoot;
	private Type type;

	public ControlBlock(Type type) {
		id = UUID.randomUUID().toString();
		status = Status.PASSIVE;
		this.type = type;
		isRoot = true;
	}
}
