package philosophers.arge.actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Data
@Accessors(chain = true)
@FieldNameConstants
public final class RouterNode implements RouterTerminator {
	private static final String OCCUPIED_TOPIC = "This topic is already occupied!!";

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<?>> rootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Integer> actorCountMap;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	@Exclude
	private ReadWriteLock lock;

	public RouterNode(ActorCluster cluster) {
		this.cluster = cluster;
		this.cb = ControlBlockFactory.createCb(ActorType.ROUTER);
		this.rootActors = new HashMap<>();
		this.actorCountMap = new HashMap<>();
		this.lock = new ReentrantReadWriteLock();
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(RouterNode.Fields.lock)
	public final void addRootActor(String topic, Actor<?> node) {
		if (rootActors.containsKey(topic))
			throw new RuntimeException(OCCUPIED_TOPIC);
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
	public final void incrementActorCount(String topic) {
		if (this.actorCountMap.containsKey(topic))
			this.actorCountMap.put(topic, this.actorCountMap.get(topic) + 1);
		else
			this.actorCountMap.put(topic, 1);
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(ActorCluster.Fields.lock)
	public final void executeNode(Actor<?> node) {
		cluster.executeNode(node);
	}

	@Override
	@NotThreadSafe
	public Map<String, List<?>> terminateRouter() {
		Map<String, List<?>> waitingJobs = new HashMap<>();
		this.cb.setStatus(Status.PASSIVE);
		for (String key : rootActors.keySet()) {
			waitingJobs.put(key, rootActors.get(key).terminate());
		}
		rootActors.clear();
		actorCountMap.clear();
		return waitingJobs;
	}
}
