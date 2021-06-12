package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@Accessors(chain = true)
public final class RouterNode implements Terminable<Object> {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private Lock lock;

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<?>> rootActors;

	@Setter(value = AccessLevel.PRIVATE)
	private Map<String, Integer> actorCount;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, Actor<?>> remoteRootActors;

	@Setter(value = AccessLevel.PRIVATE)
	@Exclude
	private ActorCluster cluster;

	public RouterNode(ActorCluster cluster) {
		this.cluster = cluster;
		this.lock = new ReentrantLock();
		this.cb = new ControlBlock(ActorType.ROUTER, Status.ACTIVE, true);
		this.rootActors = new HashMap<>();
		this.remoteRootActors = new HashMap<>();
		this.actorCount = new HashMap<>();
	}

	public final void addRootActor(String topic, Actor<?> node) {

		if (rootActors.containsKey(topic))
			throw new RuntimeException("This topic is already occupied!!");

		actorCount.put(topic, 1);
		rootActors.put(topic, node);
		node.getCb().setStatus(Status.PASSIVE);
	}

	public final Actor<?> getRootActor(String topic) {
		return rootActors.containsKey(topic) ? rootActors.get(topic) : null;
	}

	@Override
	public List<Object> terminate() {
		// save queue if neccessary!!
		getCb().setStatus(Status.PASSIVE);
		for (String key : rootActors.keySet()) {
			rootActors.get(key).terminate();
		}
		rootActors.clear();
		remoteRootActors.clear();
		return new ArrayList<>();
	}
}
