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
import philosophers.arge.actor.ControlBlock.Status;

@Data
@Accessors(chain = true)
public final class RouterNode implements RouterTerminator {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private ReadWriteLock lock;

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
	private Map<String, Actor<?>> remoteRootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	public RouterNode(ActorCluster cluster) {
		this.cluster = cluster;
		this.lock = new ReentrantReadWriteLock();
		this.cb = new ControlBlock(ActorType.ROUTER, Status.ACTIVE, true);
		this.rootActors = new HashMap<>();
		this.remoteRootActors = new HashMap<>();
		this.actorCountMap = new HashMap<>();
	}

	public final void addRootActor(String topic, Actor<?> node) {
		if (rootActors.containsKey(topic))
			throw new RuntimeException("This topic is already occupied!!");
		lock.writeLock().lock();
		try {
			this.actorCountMap.put(topic, 1);
			rootActors.put(topic, node);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public final Actor<?> getRootActor(String topic) {
		lock.readLock().lock();
		try {
			return rootActors.containsKey(topic) ? rootActors.get(topic) : null;
		} finally {
			lock.readLock().unlock();
		}
	}

	public final void incrementActorCount(String topic) {
		this.actorCountMap.put(topic, this.actorCountMap.get(topic) + 1);
	}

	@Override
	public Map<String, List<?>> terminateRouter() {
		// save queue if neccessary!!
		Map<String, List<?>> waitingJobs = new HashMap<>();
		this.cb.setStatus(Status.PASSIVE);
		for (String key : rootActors.keySet()) {
			waitingJobs.put(key, rootActors.get(key).terminate());
		}
		rootActors.clear();
		actorCountMap.clear();
		remoteRootActors.clear();
		return waitingJobs;
	}
}
