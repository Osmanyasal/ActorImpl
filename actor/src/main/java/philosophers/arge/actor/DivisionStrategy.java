package philosophers.arge.actor;

import java.util.List;

public interface DivisionStrategy<TMessage> {

	boolean isConditionValid(Actor<TMessage> actor);

	void executeSendingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message);

	void executeLoadingStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message);
}
