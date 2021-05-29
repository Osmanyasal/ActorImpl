package philosophers.arge.actor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActorMessage<T> {
	private T message;
}
