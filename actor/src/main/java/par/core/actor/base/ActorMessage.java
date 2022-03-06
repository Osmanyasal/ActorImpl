package par.core.actor.base;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;

/**
 * Immutability of the ActorMessage.message field belongs to the user.
 * 
 * @author osman.yasal
 *
 * @param <T>
 */
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
