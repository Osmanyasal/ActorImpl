package philosophers.arge.actor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.annotations.Immutable;
import philosophers.arge.actor.divisionstrategies.DivisionStrategy;

@Immutable
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ActorConfig<TMessage> {

	@Setter(AccessLevel.PRIVATE)
	private String topic;

	@Setter(AccessLevel.PRIVATE)
	private RouterNode router;

	@Setter(AccessLevel.PRIVATE)
	private DivisionStrategy<TMessage> divisionStrategy;
}
