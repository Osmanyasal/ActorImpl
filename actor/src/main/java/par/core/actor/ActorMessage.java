package par.core.actor;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;

@Immutable
@Data
@Accessors(chain = true)
public class ActorMessage<T> {
	@Setter(AccessLevel.PRIVATE)
	private String id;

	@Setter(AccessLevel.PRIVATE)
	private T message;

	public ActorMessage(T message) {
		this.id = UUID.randomUUID().toString();
		this.message = message;
	}
}
