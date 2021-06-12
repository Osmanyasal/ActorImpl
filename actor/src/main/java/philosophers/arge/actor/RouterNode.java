package philosophers.arge.actor;

import java.util.Arrays;
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
import philosophers.arge.actor.ClusterConfig.TerminationTime;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@Accessors(chain = true)
public final class RouterNode<T extends Object> implements Terminable<Object>, Callable<Boolean> {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private Lock lock;

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private long queueSize;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private List<RouterMessage<T>> queue;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<? extends Object>> rootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	@Exclude
	private TerminationTime terminationTime;

	public RouterNode(ActorCluster cluster) {
		lock = new ReentrantLock();
		cb = new ControlBlock(ActorType.ROUTER, Status.ACTIVE, true);
		getCb().setStatus(Status.ACTIVE);
		getCb().setIsRoot(true);
		queueSize = 0;
		queue = new LinkedList<>();
		rootActors = new HashMap<>();
		this.cluster = cluster;
		terminationTime = cluster.getTerminationTime();
	}

	public final <A> void addRootActor(String topic, Actor<A> node) {
		if (rootActors.containsKey(topic))
			throw new RuntimeException("This topic is already occupied!!");
		rootActors.put(topic, node);
		node.getCb().setStatus(Status.PASSIVE);
		if (node.getQueue().size() > 0) {
			node.getCb().setStatus(Status.ACTIVE);
			cluster.executeNode(node);
		}

	}

	@SuppressWarnings("rawtypes")
	public final Actor getRootActor(String topic) {
		return rootActors.containsKey(topic) ? rootActors.get(topic) : null;
	}

	public final void send(RouterMessage<T> routerMessage) {
		lock.lock();
		try {
			queue.add(routerMessage);
			queueSize++;
		} finally {
			lock.unlock();
		}
	}

	public final void sendAll(List<RouterMessage<T>> messageList) {
		lock.lock();
		try {
			messageList.stream().forEach(m -> {
				queue.add(m);
				queueSize++;
			});
		} finally {
			lock.unlock();
		}
	}

	private final RouterMessage<T> deq() {
		if (queueSize == 0)
			return null;
		queueSize--;
		return queue.remove(0);
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		while (Status.ACTIVE.equals(getCb().getStatus())) {
			if (queueSize > 0) {
				RouterMessage<?> msg = deq();
				// execute node.
				if (msg.getMessage() instanceof Actor<?>) {
					cluster.executeNode((Actor<?>) msg.getMessage());
				} else if (msg instanceof RouterMessage) {
					getRootActor(msg.getTopic()).send(new ActorMessage<Object>().setMessage(msg.getMessage()));
				} else {
					System.out.println("default message : " + msg.getMessage());
				}
			}
			try {
				// sleep 5ms before next iteration.
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Router Terminated!!");
	}

	@Override
	public List<Object> terminate() {
		// save queue if neccessary!!
		try {
			getCb().setStatus(Status.PASSIVE);
			queue.clear();
			queueSize = 0;
			for (String key : rootActors.keySet()) {
				rootActors.get(key).terminate();
			}
			rootActors.clear();
			cluster = null;
			return Arrays.asList(0);
		} catch (Exception e) {
			return Arrays.asList(0);
		}
	}

	@Override
	public Boolean call() throws Exception {
		execute();
		return true;
	}
}
