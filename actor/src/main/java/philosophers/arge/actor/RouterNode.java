package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import philosophers.arge.actor.exceptions.OccupiedTopicException;

@Data
@Accessors(chain = true)
@FieldNameConstants
public final class RouterNode implements RouterTerminator {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Integer> actorCountMap;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<?>> rootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	@Exclude
	private ReadWriteLock lock;

	public RouterNode(ActorCluster cluster) {
		init(cluster);
	}

	private void init(ActorCluster cluster) {
		this.cb = ControlBlockFactory.createCb(ActorType.ROUTER);
		this.cluster = cluster;
		this.rootActors = new HashMap<>();
		this.actorCountMap = new HashMap<>();
		this.lock = new ReentrantReadWriteLock();
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(RouterNode.Fields.lock)
	public final void addRootActor(String topic, Actor<?> node) {
		if (rootActors.containsKey(topic))
			throw new OccupiedTopicException();
		lock.writeLock().lock();
		try {
			incrementActorCount(topic);
			rootActors.put(topic, node);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(RouterNode.Fields.lock)
	// normally it's not a best practice to return wildcard type but!
	// since the implementer knows that what kind of root actor is calling
	// we let the implentor to convert the returning actor.
	public final Actor<?> getRootActor(String topic) {
		lock.readLock().lock();
		try {
			return rootActors.containsKey(topic) ? rootActors.get(topic) : null;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Immutable
	@NotThreadSafe
	public final Set<String> getAllTopics() {
		return rootActors.keySet();
	}

	@Immutable
	@NotThreadSafe
	protected final void incrementActorCount(String topic) {
		if (this.actorCountMap.containsKey(topic))
			this.actorCountMap.put(topic, this.actorCountMap.get(topic) + 1);
		else
			this.actorCountMap.put(topic, 1);
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.poolLock)
	public final void executeNode(Actor<?> node) {
		cluster.executeNode(node);
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
	@NotThreadSafe
	public Map<String, List<?>> terminateRouter() {
		Map<String, List<?>> waitingJobs = new HashMap<>();
		for (String key : rootActors.keySet()) {
			waitingJobs.put(key, rootActors.get(key).terminate());
		}
		rootActors.clear();
		actorCountMap.clear();
		this.cb.setStatus(Status.PASSIVE);
		return waitingJobs;
	}
}
