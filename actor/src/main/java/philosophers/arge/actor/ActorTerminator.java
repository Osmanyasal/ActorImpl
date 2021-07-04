package philosophers.arge.actor;

import java.util.List;

public interface ActorTerminator<T> extends Terminable {
	List<ActorMessage<T>> terminate();
}