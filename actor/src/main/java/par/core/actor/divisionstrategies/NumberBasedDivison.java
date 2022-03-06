package par.core.actor.divisionstrategies;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;
import par.core.actor.base.ActorMessage;
import par.core.actor.base.node.Actor;

/**
 * If the queue size exceeds our queue limit we create a new child actor and
 * deliver incomming messages to it.
 *
 * @author osmanyasal
 *
 * @param <T> : actor message type
 */
@Immutable
@Data
@Accessors(chain = true)
@AllArgsConstructor
public final class NumberBasedDivison<T> implements DivisionStrategy<T> {
	@Setter(AccessLevel.PRIVATE)
	private Long queueLimit;

	@Override
	public boolean isConditionValid(Actor<T> actor) {
		return actor.getQueueSize() >= queueLimit;
	}

	@Override
	public void executeSendingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		actor.fetchChildActor().sendAllByLocking(message);
	}

	@Override
	public void executeLoadingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		actor.fetchChildActor().loadAll(message);
	}
}
