package philosophers.arge.actor.terminators;

import java.util.List;

import philosophers.arge.actor.ActorMessage;

public interface ActorTerminator<T> extends Terminable {
	List<ActorMessage<T>> terminate();
}