package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ClusterConfig.TerminationTime;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@Accessors(chain = true)
public class ActorCluster implements Terminable {
	private Lock lock;
	private String name;
	private RouterNode<Object> router;
	private Object gateway;
	private ExecutorService pool;
	private Map<String, List<Future<?>>> futures;
	private ControlBlock cb;
	private TerminationTime terminationTime;

	public ActorCluster(ClusterConfig config) {
		init();
		adjustConfigurations(config);
	}

	private void init() {
		this.cb = new ControlBlock(ActorType.CLUSTER, Status.ACTIVE, true);
		this.futures = new HashMap<>();
		this.lock = new ReentrantLock();
		router = new RouterNode<Object>(this);
		futures.put(ActorType.ROUTER.name(), new ArrayList<>());
		futures.get(ActorType.ROUTER.name()).add(pool.submit(router));
	}

	private void adjustConfigurations(ClusterConfig config) {
		this.name = config.getName();
		this.pool = Executors.newFixedThreadPool(config.getThreadCount());
		this.terminationTime = config.getTerminationTime();
	}

	public int getActiveNodeCount(String topic) {
		lock.lock();
		try {
			return (int) (futures.containsKey(topic)
					? getRouter().getQueueSize() + futures.get(topic).stream().filter(x -> !x.isDone()).count() - 1
					: 0);
		} finally {
			lock.unlock();
		}
	}

	public int getActiveNodeCount() {
		lock.lock();
		try {
			int count = (int) getRouter().getQueueSize();
			for (List<Future<?>> tList : futures.values()) {
				count += tList.stream().filter(x -> !x.isDone()).count();
			}
			return count - 1;
		} finally {
			lock.unlock();
		}
	}

	public void removeFuture(String topicName) {
		lock.lock();
		try {

			if (futures.containsKey(topicName))
				futures.remove(topicName);
		} finally {
			lock.unlock();
		}
	}

	public <T> void executeNode(Actor<T> node) {
		if (node.getCb().getStatus().equals(Status.PASSIVE)) {
			node.getCb().setStatus(Status.ACTIVE);
			lock.lock();
			try {
				if (futures.containsKey(node.getTopic()))
					futures.get(node.getTopic()).add(pool.submit(node));
				else {
					List<Future<?>> futureList = new ArrayList<>();
					futureList.add(pool.submit(node));
					futures.put(node.getTopic(), futureList);
				}
			} finally {
				lock.unlock();
			}
		}
	}

	// TODO: Make Termination process better.
	public boolean terminate() {
		try {
			// do not make it asapp !!
			terminateRouter();
			terminateThreadPool();
			getCb().setStatus(Status.PASSIVE);
			System.out.println("cluster terminated!");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void waitTermination() throws InterruptedException {

	}

	public <T> void addRootActor(Actor<T> node) {
		router.addRootActor(node.getTopic(), node);
	}

	private void terminateThreadPool() throws InterruptedException {
		pool.shutdown();
		try {
			pool.awaitTermination(2, TimeUnit.SECONDS);
		} finally {
			if (!pool.isTerminated())
				pool.shutdownNow();
		}
	}

	private void terminateRouter() {
		router.terminate();
	}
}
