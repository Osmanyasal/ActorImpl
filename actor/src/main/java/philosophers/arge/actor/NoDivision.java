package philosophers.arge.actor;

import java.util.List;

public final class NoDivision<TMessage> implements DivisionStrategy<TMessage> {

	@Override
	public boolean isConditionValid(Actor<TMessage> actor) {
		return false;
	}


	@Override
	public void executeSendingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message) {
	}

	@Override
	public void executeLoadingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message) {
	}

}
