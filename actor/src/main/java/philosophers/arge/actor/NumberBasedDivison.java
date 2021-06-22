package philosophers.arge.actor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * In this strategy, if the queue size exceeds our queue limit we create a new
 * child actor and toast messages to it.
 *
 * @author osmanyasal
 *
 * @param <TMessage>
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public final class NumberBasedDivison<TMessage> implements DivisionStrategy<TMessage> {
	private Long queueLimit;

	@Override
	public boolean isConditionValid(Actor<TMessage> actor) {
		return actor.getQueue().size() >= queueLimit;
	}

	@Override
	public void executeSendingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message) {
		actor.fetchChildActor().sendAllByLocking(message);
	}

	@Override
	public void executeLoadingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message) {
		actor.fetchChildActor().loadAll(message);
	}
}
