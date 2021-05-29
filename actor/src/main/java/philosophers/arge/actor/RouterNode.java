package philosophers.arge.actor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@Accessors(chain = true)
public final class RouterNode<TMessage extends RouterMessage<?>> implements Callable<Object> {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private Lock lock;

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private long queueSize;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private List<TMessage> queue;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, ActorNode<Object>> rootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	public RouterNode(ActorCluster cluster) {
		lock = new ReentrantLock();
		cb = new ControlBlock(Type.ROUTER);
		getCb().setStatus(Status.ACTIVE);
		getCb().setIsRoot(true);
		queueSize = 0;
		queue = new LinkedList<>();
		rootActors = new HashMap<String, ActorNode<Object>>();
		this.cluster = cluster;
	}

	public final void addRootActor(String topic, ActorNode<Object> node) {
		if (rootActors.containsKey(topic))
			throw new RuntimeException("This topic is already occupied!!");
		rootActors.put(topic, node);
		if (node.getQueueSize() > 0)
			cluster.executeNode(node);
	}

	public final ActorNode<Object> getRootActor(String topic) {
		return rootActors.containsKey(topic) ? rootActors.get(topic) : null;
	}

	public final void send(TMessage routerMessage) {
		lock.lock();
		try {
			queue.add(routerMessage);
			queueSize++;
		} finally {
			lock.unlock();
		}
	}

	public final void sendAll(List<TMessage> messageList) {
		lock.lock();
		try {
			messageList.stream().forEach(x -> {
				queue.add(x);
				queueSize++;
			});
		} finally {
			lock.unlock();
		}

	}

	public final TMessage deq() {
		lock.lock();
		try {
			if (queueSize == 0)
				return null;
			queueSize--;
			return queue.remove(0);
		} finally {
			lock.unlock();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object call() throws Exception {
		while (Status.ACTIVE.equals(cb.getStatus())) {
			if (queueSize > 0) {
				TMessage msg = deq();
				if (msg.getMessage() instanceof ActorNode) {
					cluster.executeNode((ActorNode<TMessage>) msg.getMessage());
				} else if (msg instanceof RouterMessage) {
					getRootActor(msg.getTopic()).send(new ActorMessage<>().setMessage(msg.getMessage()));
				}
			}
		}
		return null;
	}
}
