package philosophers.arge.actor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ActorConfig<TMessage> {
	private String topic;
	private RouterNode router;
	private ActorPriority priority;
	private DivisionStrategy<TMessage> divisionStrategy;
}
