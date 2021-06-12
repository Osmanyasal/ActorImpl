package philosophers.arge.actor;

import java.util.List;

public interface DivisionStrategy<TMessage> {

	boolean isConditionValid(Actor<TMessage> actor);

	void executeStrategy(Actor<TMessage> actor, List<ActorMessage<TMessage>> message);
}
