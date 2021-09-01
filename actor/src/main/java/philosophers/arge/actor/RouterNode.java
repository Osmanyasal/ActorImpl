package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import philosophers.arge.actor.annotations.NotImplemented;
import philosophers.arge.actor.annotations.ThreadSafe;
import philosophers.arge.actor.cache.Cache;
import philosophers.arge.actor.exceptions.OccupiedTopicException;
import philosophers.arge.actor.serializers.JsonSeriliazer;
import philosophers.arge.actor.terminators.RouterTerminator;

@Data
@Accessors(chain = true)
@FieldNameConstants
public final class RouterNode implements RouterTerminator, JsonSeriliazer {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Integer> actorCountMap;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<?>> rootActors;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private ActorCluster cluster;

	@Exclude
	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Logger logger;

	public RouterNode(ActorCluster cluster) {
		init(cluster);
	}

	private void init(ActorCluster cluster) {
		this.logger = LogManager.getLogger(RouterNode.class);
		this.cb = ControlBlockFactory.createCb(ActorType.ROUTER);
		this.cluster = cluster;
		this.actorCountMap = new HashMap<>();

		// rootActors must be ordered because of watinig-actors see Actor.class
		this.rootActors = new ConcurrentHashMap<>(new LinkedHashMap<>());
	}

	@Immutable
	@ThreadSafe
	@GuardedBy("concurrentHashMap")
	public final void addRootActor(Topic topic, Actor<?> node) {
		if (rootActors.containsKey(topic.getName()))
			throw new OccupiedTopicException();
		rootActors.put(topic.getName(), node);
		incrementActorCount(topic);
	}

	@Immutable
	@ThreadSafe
	@GuardedBy("concurrentHashMap")
	// normally it's not a best practice to return wildcard type but!
	// since the implementer knows that what kind of root actor is calling
	// we let the implentor to convert the returning actor.
	public final Actor<?> getRootActor(String topic) {
		return isTopicExists(topic) ? rootActors.get(topic) : null;
	}

	@Immutable
	@ThreadSafe
	public final boolean isTopicExists(String topic) {
		return rootActors.containsKey(topic);
	}

	@Immutable
	public final List<String> getAllTopics() {
		List<String> result = new ArrayList<>(rootActors.keySet());
		Collections.reverse(result);
		return result;
	}

	@Immutable
	protected final void incrementActorCount(Topic topic) {
		if (this.actorCountMap.containsKey(topic.getName()))
			this.actorCountMap.put(topic.getName(), this.actorCountMap.get(topic.getName()) + 1);
		else
			this.actorCountMap.put(topic.getName(), 1);
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.poolLock)
	public final void executeNode(Actor<?> node) {
		cluster.executeNode(node);
	}

	@Immutable
	@ThreadSafe
	public final Cache getDelayedCache() {
		return cluster.getCache();
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
	public Map<String, List<?>> terminateRouter() {
		Map<String, List<?>> waitingJobs = new HashMap<>();
		for (String key : rootActors.keySet()) {
			waitingJobs.put(key, rootActors.get(key).terminateActor());
		}
		rootActors.clear();
		actorCountMap.clear();
		this.cb.setStatus(Status.PASSIVE);
		return waitingJobs;
	}

	public final void waitForTermination(final List<String> topicList, boolean showInfo) throws Exception {
		if (CollectionUtils.isEmpty(topicList))
			return;
		for (int i = 0; i < topicList.size(); i++) {
			cluster.waitForTermination(topicList.get(i), showInfo);
		}
	}

	/**
	 * Terminates the given topic with all its nodes. returns the waiting jobs
	 * attached to that topic.
	 * 
	 * @param topic
	 * @return
	 */
	public Map<String, List<?>> terminateTopic(final Topic topic) {
		Map<String, List<?>> waitingJobs = new HashMap<>();
		waitingJobs.put(topic.getName(), rootActors.get(topic.getName()).terminateActor());
		return waitingJobs;
	}

	@Override
	public String toJson() {
		Gson gson = new GsonBuilder().create();
		Map<String, String> map = new HashMap<>();
		for (String actor : rootActors.keySet()) {
			map.put(actor, rootActors.get(actor).toJson());
		}
		return gson.toJson(map);
	}
}
