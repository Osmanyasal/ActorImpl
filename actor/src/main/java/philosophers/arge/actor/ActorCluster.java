package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Arrays;
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

	public ActorCluster(ClusterConfig config) {
		this.name = config.getName();
		this.pool = Executors.newFixedThreadPool(config.getThreadCount());
		this.futures = new HashMap<>();
		this.cb = new ControlBlock(ActorType.CLUSTER, Status.ACTIVE, true);

		this.lock = new ReentrantLock();
		this.router = new RouterNode<>(this);
		futures.put(ActorType.ROUTER.name(), Arrays.asList(pool.submit(router)));
	}

	public int getRemainingJobs(String topic) {
		synchronized (futures) {
			return (int) (futures.containsKey(topic) ? futures.get(topic).stream().filter(x -> !x.isDone()).count()
					: 0);
		}
	}

	public <T> void addRootActor(Actor<T> node) {
		router.addRootActor(node.getTopic(), node);
	}

	public void removeFuture(String topicName) {
		if (futures.containsKey(topicName))
			futures.remove(topicName);
	}

	public <T> void executeNode(Actor<T> node) {
		if (node.getCb().getStatus().equals(Status.PASSIVE)) {
			node.getCb().setStatus(Status.ACTIVE);
			if (futures.containsKey(node.getTopic()))
				futures.get(node.getTopic()).add(pool.submit(node));
			else {
				List<Future<?>> futureList = new ArrayList<>();
				futureList.add(pool.submit(node));
				futures.put(node.getTopic(), futureList);
			}
		}
	}

	public boolean terminate() {
		try {
			// do not make it asapp !!
			Thread.sleep(1000);
			terminateRouter();
			terminateThreadPool();
			return true;
		} catch (Exception e) {
			return false;
		}
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
