package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import philosophers.arge.actor.ClusterConfig.TerminationTime;
import philosophers.arge.actor.ControlBlock.Status;
import philosophers.arge.actor.annotations.GuardedBy;
import philosophers.arge.actor.annotations.Immutable;
import philosophers.arge.actor.annotations.NotThreadSafe;
import philosophers.arge.actor.annotations.ThreadSafe;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ActorCluster implements ClusterTerminator {
	private final String terminatedMessage;
	private String name;
	private ControlBlock cb;
	private RouterNode router;
	private Object gateway;

	private Map<String, List<Future<?>>> futures;

	private TerminationTime terminationTime;

	private Lock poolLock;

	@Exclude
	private ExecutorService pool;

	public ActorCluster(ClusterConfig config) {
		terminatedMessage = String.format("Cluster '%s' Terminated!", config.getName());
		adjustConfigurations(config);
		init();
		System.out.println(config);
	}

	@Immutable
	private final void init() {
		this.cb = new ControlBlock(ActorType.CLUSTER, Status.ACTIVE, true);
		this.futures = new HashMap<>();
		this.poolLock = new ReentrantLock();
		this.router = new RouterNode(this);
	}

	@Immutable
	private final void adjustConfigurations(ClusterConfig config) {
		this.name = config.getName();
		this.pool = Executors.newFixedThreadPool(config.getThreadCount());
		this.terminationTime = config.getTerminationTime();
	}

	@Immutable
	@NotThreadSafe
	public final int getActiveNodeCount(String topic) {
		return router.getRootActor(topic).getActiveNodeCount();
	}

	@Immutable
	@NotThreadSafe
	public final int getActiveNodeCount() {
		return 0;
	}

	@Immutable
	@NotThreadSafe
	public final int getNodeCount(String topic) {
		int count = 0;
		Actor<?> actor = this.router.getRootActor(topic);
		while (actor != null) {
			count++;
			actor = actor.getChildActor();
		}
		return count;
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.poolLock)
	public final void executeNode(Actor<?> node) {
		if (Status.PASSIVE.equals(node.getCb().getStatus())) {
			node.getCb().setStatus(Status.ACTIVE);
			poolLock.lock();
			try {
				if (futures.containsKey(node.getTopic()))
					futures.get(node.getTopic()).add(pool.submit(node));
				else {
					List<Future<?>> futureList = new ArrayList<>();
					futureList.add(pool.submit(node));
					futures.put(node.getTopic(), futureList);
				}
			} finally {
				poolLock.unlock();
			}
		}
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(RouterNode.Fields.lock)
	public final <T> void addRootActor(Actor<T> node) {
		router.addRootActor(node.getTopic(), node);
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.poolLock)
	public void abortThreadPoolTasks() throws InterruptedException {
		poolLock.lock();
		try {
			for (String key : futures.keySet()) {
				futures.get(key).forEach(x -> x.cancel(true));
			}

		} finally {
			poolLock.unlock();
		}
	}

	private List<Runnable> terminateThreadPool() {
		pool.shutdown();
		try {
			pool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	 * <br>
	 * } <br>
	 */
	@Override
	public Map<String, List<?>> terminateCluster(boolean isPermenent, boolean showInfo) {
		Map<String, List<?>> result = null;
		try {
			// aborting thread pool tasks triggers interruption to related thread.
			// once a task is aborted while it's executed by the pool, we try to add the
			// task to the end of the queue
			// this process is about saving currently executing task.
			abortThreadPoolTasks();

			// in order to propogate trigger effect amongs other threads we wait few ms.
			Thread.sleep(5);

			// and collect the queue values.
			result = this.router.terminateRouter();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (isPermenent)
				terminateThreadPool();
			this.cb.setStatus(Status.PASSIVE);
			if (showInfo)
				System.out.println(terminatedMessage);
		}
		return result;
	}

	@Immutable
	public final void waitForTermination(boolean showInfo) throws InterruptedException {

		ArrayList<String> allTopics = new ArrayList<>(router.getAllTopics());
		for (int i = 0; i < allTopics.size(); i++) {
			if (!waitForTermination(allTopics.get(i), showInfo))
				i--;
		}
		if (showInfo)
			System.out.println("All tasks are done!");
		System.gc();
	}

	@Immutable
	public final boolean waitForTermination(String topic, boolean showInfo) throws InterruptedException {
		Actor<?> rootActor = router.getRootActor(topic);
		if (rootActor == null)
			return false;

		boolean isAllTerminated = false;
		Actor<?> temp;
		do {
			temp = rootActor;
			while (temp != null) {
				isAllTerminated = isAllTerminated || Status.PASSIVE.equals(temp.getCb().getStatus());
				temp = temp.getChildActor();
				if (isAllTerminated)
					break;
			}

			// sleep for 5ms
			Thread.sleep(5);
		} while (!isAllTerminated);
		if (showInfo)
			System.out.println(topic + " tasks are done!");
		System.gc();
		return true;
	}
}
