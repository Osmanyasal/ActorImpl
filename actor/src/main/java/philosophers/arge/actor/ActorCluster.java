package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import philosophers.arge.actor.ControlBlock.Status;
import philosophers.arge.actor.annotations.GuardedBy;
import philosophers.arge.actor.annotations.Immutable;
import philosophers.arge.actor.annotations.NotThreadSafe;
import philosophers.arge.actor.annotations.ThreadSafe;
import philosophers.arge.actor.cache.DelayedCache;
import philosophers.arge.actor.configs.ClusterConfig;
import philosophers.arge.actor.exceptions.InvalidTopicException;
import philosophers.arge.actor.serializers.JsonSeriliazer;
import philosophers.arge.actor.terminators.ClusterTerminator;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class ActorCluster implements ClusterTerminator, JsonSeriliazer {
	private final String terminatedMessage;
	private final String allTasksAreDoneMessage;
	@Setter(AccessLevel.PRIVATE)
	private String name;

	@Setter(AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(AccessLevel.PRIVATE)
	private RouterNode router;

	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Object gateway;

	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Map<String, List<Future<?>>> futures;

	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Lock poolLock;

	@Setter(AccessLevel.PRIVATE)
	private DelayedCache cache;

	@Exclude
	@Setter(AccessLevel.PRIVATE)
	private ExecutorService pool;

	@Exclude
	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private ClusterConfig config;

	@Exclude
	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Logger logger;

	public ActorCluster(ClusterConfig config) {
		this.config = config;
		allTasksAreDoneMessage = "All tasks are done!";
		terminatedMessage = String.format("Cluster '%s' Terminated!", config.getName());
		adjustConfigurations(config);
		init();
		System.out.println(config);
	}

	@Immutable
	private final void init() {
		this.logger = LogManager.getLogger(ActorCluster.class);
		this.futures = new HashMap<>();
		this.poolLock = new ReentrantLock();
		this.router = new RouterNode(this);
		this.cache = new DelayedCache();
	}

	@Immutable
	private final void adjustConfigurations(ClusterConfig config) {
		this.cb = new ControlBlock(config.isDeamon() ? ActorType.DEAMON : ActorType.CLUSTER, Status.ACTIVE, true);
		this.name = config.getName();
		this.pool = ExecutorFactory.getExecutor(config.getPoolType(), config.getThreadCount());
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
				if (futures.containsKey(node.getTopic().getName()))
					futures.get(node.getTopic().getName()).add(pool.submit(node));
				else {
					List<Future<?>> futureList = new ArrayList<>();
					futureList.add(pool.submit(node));
					futures.put(node.getTopic().getName(), futureList);
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
	public final void abortThreadPoolTasks() {
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
			logger.warn(e.getStackTrace());
		} finally {
			if (!pool.isTerminated()) {
				return pool.shutdownNow();
			}
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
		Map<String, List<?>> result = new HashMap<>();
		try {
			// aborting thread pool tasks triggers interruption to related thread.
			// once a task is aborted while it's executed by the pool, we try to add the
			// task to the end of the queue.
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
				result.put("Pool_Waiting_Queue", terminateThreadPool());
			this.cb.setStatus(Status.PASSIVE);
			if (showInfo)
				logger.info(terminatedMessage);
			System.gc();
		}
		return result;
	}

	@Immutable
	@NotThreadSafe
	public final void waitForTermination(boolean showInfo) throws Exception {

		List<String> allTopics = router.getAllTopics();
		System.out.println(allTopics);
		for (int i = 0; i < allTopics.size(); i++) {
			waitForTermination(allTopics.get(i), showInfo);
		}
		if (showInfo)
			logger.info(allTasksAreDoneMessage);
		System.gc();
	}

	@Immutable
	public final boolean waitForTermination(String topic, boolean showInfo) throws Exception {
		if (!router.isTopicExists(topic))
			throw new InvalidTopicException(topic);

		Actor<?> rootActor = router.getRootActor(topic);
		boolean isAllTerminated = true;
		do {
			isAllTerminated = true;
			Actor<?> temp;
			temp = rootActor;
			while (temp != null) {
				isAllTerminated = isAllTerminated && Status.PASSIVE.equals(temp.getCb().getStatus());
				temp = temp.getChildActor();
			}
			// sleep for 5ms
			Thread.sleep(5);
		} while (!isAllTerminated);
		if (showInfo)
			logger.info((topic + " " + allTasksAreDoneMessage));
		System.gc();
		return true;
	}

	@Override
	public final String toJson() {
		Gson gson = new GsonBuilder().create();
		Map<String, String> keyValuePairs = new HashMap<String, String>();
		keyValuePairs.put(ActorCluster.class.getSimpleName(), gson.toJson(getConfig()));
		keyValuePairs.putAll(gson.fromJson(getRouter().toJson(), new TypeToken<Map<String, String>>() {
		}.getType()));
		return gson.toJson(keyValuePairs);
	}
}
