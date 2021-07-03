package philosophers.arge.actor;

import java.util.List;

public interface ActorTerminator<TMessage> extends Terminable {
	List<ActorMessage<TMessage>> terminate();
}