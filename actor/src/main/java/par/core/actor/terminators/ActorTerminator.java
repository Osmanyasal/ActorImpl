package par.core.actor.terminators;

import java.util.List;

import par.core.actor.ActorMessage;

public interface ActorTerminator<T> extends Terminable {
	List<ActorMessage<T>> terminateActor();
}