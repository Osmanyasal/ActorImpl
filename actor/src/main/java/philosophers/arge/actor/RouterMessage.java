package philosophers.arge.actor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RouterMessage<T> {
	private T message;
	private String topic;
}
