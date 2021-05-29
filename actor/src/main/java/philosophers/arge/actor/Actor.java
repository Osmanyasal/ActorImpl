package philosophers.arge.actor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class Actor<TMessage extends Object> extends ActorMessage<TMessage> implements Callable<Object> {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private long queueSize;

	@Setter(value = AccessLevel.PRIVATE)
	private List<ActorMessage<TMessage>> queue;

	@Setter(value = AccessLevel.PRIVATE)
	private Actor<TMessage> childActor;

	private String topic;

	private RouterNode<RouterMessage<Object>> router;

	protected Actor(Type type, RouterNode<RouterMessage<Object>> router) {
		cb = new ControlBlock(type);
		queue = new LinkedList<>();
		queueSize = 0;
		this.router = router;
	}

	public final void send(ActorMessage<TMessage> message) {
		queue.add(message);
		queueSize++;
		notifyRouter();
	}

	public final void sendAll(List<ActorMessage<TMessage>> messageList) {
		messageList.stream().forEach(x -> {
			queue.add(x);
			queueSize++;
		});
		notifyRouter();
	}

	public void notifyRouter() {
		router.send(new RouterMessage<Object>().setMessage(this));
	}

	public final ActorMessage<TMessage> deq() {
		if (queueSize == 0)
			return new ActorMessage<TMessage>();
		queueSize--;
		return queue.remove(0);
	}

	public final Actor<TMessage> generateActor(String topic, RouterNode<RouterMessage<Object>> router) {
		Actor<TMessage> node = new ActorNode<TMessage>(topic, router);
		node.getCb().setIsRoot(false);
		childActor = node;
		return node;
	}

	@Override
	public Object call() throws Exception {
		operate();
		return null;
	}

	public abstract void operate();

}
