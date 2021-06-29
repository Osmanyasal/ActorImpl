package philosophers.arge.actor.divisionstrategies;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import philosophers.arge.actor.Actor;
import philosophers.arge.actor.ActorMessage;
import philosophers.arge.actor.annotations.Immutable;

@Immutable
@Data
@Accessors(chain = true)
public final class NoDivision<T> implements DivisionStrategy<T> {

	@Override
	public boolean isConditionValid(Actor<T> actor) {
		return false;
	}

	@Override
	public void executeSendingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		return;
	}

	@Override
	public void executeLoadingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		return;
	}
}
