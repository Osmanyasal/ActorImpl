package par.core.actor.terminators;

import java.util.List;

import par.core.actor.base.ActorMessage;

public interface ActorTerminator<T> extends Terminable {
	List<ActorMessage<T>> terminateActor();
}