package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class ActorCluster implements ClusterTerminator {
	private String name;
	private ControlBlock cb;
	private RouterNode router;
	private Object gateway;
	private Map<String, List<Future<?>>> futures;
	private TerminationTime terminationTime;
	private Lock lock;
	private ExecutorService pool;

	public ActorCluster(ClusterConfig config) {
		System.out.println(config);
		adjustConfigurations(config);
		init();
	}

	private final void init() {
		this.cb = new ControlBlock(ActorType.CLUSTER, Status.ACTIVE, true);
		this.futures = new HashMap<>();
		this.lock = new ReentrantLock();
		this.router = new RouterNode(this);
	}

	private final void adjustConfigurations(ClusterConfig config) {
		this.name = config.getName();
		this.pool = Executors.newFixedThreadPool(config.getThreadCount());
		this.terminationTime = config.getTerminationTime();
	}

	public final int getActiveNodeCount(String topic) {
		return 0;
	}

	public final int getActiveNodeCount() {
		return 0;
	}

	public final int getNodeCount(String topic) {
		int count = 0;
		Actor<?> actor = this.router.getRootActor(topic);
		while (actor != null) {
			count++;
			actor = actor.getChildActor();
		}
		return count;
	}

	public final void executeNode(Actor<?> node) {
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

	public final <T> void addRootActor(Actor<T> node) {
		router.addRootActor(node.getTopic(), node);
	}

	private List<Runnable> terminateThreadPool() throws InterruptedException {
		pool.shutdown();
		try {
			pool.awaitTermination(2, TimeUnit.SECONDS);
		} finally {
			if (!pool.isTerminated())
				return pool.shutdownNow();
		}
		return Collections.emptyList();
	}

	/**
	 * returns Map<String, List<?>> <br>
	 * ex: <br>
	 * { <br>
	 * "node1" : [ActorMessage(msg = "msg1"),ActorMessage(msg = "msg2")], <br>
	 * "node2" : [ActorMessage(msg = 5),ActorMessage(msg = 382)], <br>
	 * "pool" : [Callable(...),Callable(...)], <br>
	 * <br>
	 * } <br>
	 */
	@Override
	public Map<String, List<?>> terminateCluster() {
		Map<String, List<?>> result = null;
		try {
			result = this.router.terminateRouter();
			result.put("pool", terminateThreadPool());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.cb.setStatus(Status.PASSIVE);
			System.out.println("cluster terminated");
		}
		return result;
	}

	// TODO:Bitip bitmediği bilgisi root dugumlerden sorulsun
	public final void waitForTermination() throws InterruptedException {
		Collection<List<Future<?>>> values = getFutures().values();
		while (values.parallelStream().anyMatch(x -> x.stream().anyMatch(m -> !m.isDone())))
			Thread.sleep(10);
		System.out.println("All tasks are done!");
	}

	public final void waitForTermination(String topic) throws InterruptedException {
		List<Future<?>> list = getFutures().get(topic);
		while (list.parallelStream().anyMatch(x -> !x.isDone()))
			Thread.sleep(10);
		System.out.println(topic + " tasks are done!");
	}
}
