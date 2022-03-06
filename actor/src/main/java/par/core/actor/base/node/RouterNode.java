package par.core.actor.base.node;

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
import par.core.actor.annotations.GuardedBy;
import par.core.actor.annotations.ThreadSafe;
import par.core.actor.base.ControlBlock;
import par.core.actor.base.ControlBlock.Status;
import par.core.actor.base.Topic;
import par.core.actor.base.Type;
import par.core.actor.cache.Cache;
import par.core.actor.exceptions.OccupiedTopicException;
import par.core.actor.factories.ControlBlockFactory;
import par.core.actor.serializers.JsonSeriliazer;
import par.core.actor.terminators.RouterTerminator;

@Data
@Accessors(chain = true)
@FieldNameConstants
public final class RouterNode implements RouterTerminator, JsonSeriliazer {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	//Hold actor count based on topics
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Integer> actorCountMap;

	
	//Hold root actors based on theirs topic
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
		this.cb = ControlBlockFactory.createCb(Type.ROUTER);
		this.cluster = cluster;
		this.actorCountMap = new HashMap<>();

		// rootActors must be ordered because of watinig-actors @see Actor.class
		this.rootActors = new ConcurrentHashMap<>(new LinkedHashMap<>());
	}

	@ThreadSafe
	@GuardedBy("concurrentHashMap")
	public final void addRootActor(Topic topic, Actor<?> node) {
		if (rootActors.containsKey(topic.getName()))
			throw new OccupiedTopicException();
		rootActors.put(topic.getName(), node);
		incrementActorCount(topic);
	}

	@ThreadSafe
	@GuardedBy("concurrentHashMap")
	// normally it's not a best practice to return wildcard type but!
	// since the implementer knows that what kind of root actor is calling
	// we let the implentor to convert the returning actor.
	public final Actor<?> getRootActor(String topic) {
		return isTopicExists(topic) ? rootActors.get(topic) : null;
	}

	@ThreadSafe
	@GuardedBy("concurrentHashMap")
	public final boolean isTopicExists(String topic) {
		return rootActors.containsKey(topic);
	}

	public final List<String> getAllTopics() {
		List<String> result = new ArrayList<>(rootActors.keySet());
		Collections.reverse(result);
		return result;
	}

	protected final void incrementActorCount(Topic topic) {
		if (this.actorCountMap.containsKey(topic.getName()))
			this.actorCountMap.put(topic.getName(), this.actorCountMap.get(topic.getName()) + 1);
		else
			this.actorCountMap.put(topic.getName(), 1);
	}

	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.poolLock)
	public final void executeNode(Actor<?> node) {
		cluster.executeNode(node);
	}

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
			waitingJobs.put(key, rootActors.get(key).terminateActor(true));
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
		waitingJobs.put(topic.getName(), rootActors.get(topic.getName()).terminateActor(true));
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
