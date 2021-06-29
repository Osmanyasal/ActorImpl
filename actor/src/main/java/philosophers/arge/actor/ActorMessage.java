package philosophers.arge.actor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.annotations.Immutable;

@Immutable
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ActorMessage<T> {
	@Setter(AccessLevel.PRIVATE)
	private T message;
}
