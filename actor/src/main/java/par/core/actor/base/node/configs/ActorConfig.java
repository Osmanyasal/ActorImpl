package par.core.actor.base.node.configs;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;
import par.core.actor.base.ActorPriority;
import par.core.actor.base.Topic;
import par.core.actor.base.node.RouterNode;
import par.core.actor.divisionstrategies.DivisionStrategy;

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
