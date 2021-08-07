package philosophers.arge.actor.configs;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ActorPriority;
import philosophers.arge.actor.RouterNode;
import philosophers.arge.actor.Topic;
import philosophers.arge.actor.annotations.Immutable;
import philosophers.arge.actor.divisionstrategies.DivisionStrategy;

@Immutable
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ActorConfig<T> {

	@Setter(AccessLevel.PRIVATE)
	private Topic topic;

	@Setter(AccessLevel.PRIVATE)
	private transient RouterNode router;

	@Setter(AccessLevel.PRIVATE)
	private DivisionStrategy<T> divisionStrategy;

	@Setter(AccessLevel.PRIVATE)
	private ActorPriority priority;

	@Setter(AccessLevel.PRIVATE)
	private List<Topic> waitList;
}
