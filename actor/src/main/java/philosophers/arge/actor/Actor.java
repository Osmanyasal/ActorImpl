package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.util.CollectionUtils;

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
import philosophers.arge.actor.cache.Cache;
import philosophers.arge.actor.configs.ActorConfig;
import philosophers.arge.actor.divisionstrategies.DivisionStrategy;
import philosophers.arge.actor.terminators.ActorTerminator;

/**
 * Actors itself is {@code NotThreadSafe} because every actor has it's data to
 * process and runs on a thread.<br>
 * Only some <b>enq()</b> and <b>deq()</b> methods are designed as
 * {@code ThreadSafe} to prevent ambiguity on message passing from other actors
 * 
 * @author osmanyasal
 *
 * @param <T>
 */
@ThreadSafe
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class Actor<T> implements Callable<Object>, ActorTerminator<T>, Comparable<Actor<T>> {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private Topic topic;

	@Setter(value = AccessLevel.PRIVATE)
	private ActorPriority priority;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private List<ActorMessage<T>> queue;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private RouterNode router;

	/**
	 * Every actor might have only one child actor.
	 */
	@Setter(value = AccessLevel.PRIVATE)
	private Actor<T> childActor;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private boolean isNotified;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Lock queueLock;

	private DivisionStrategy<T> divisionStrategy;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private List<Actor<?>> waitList;

	@Override
	public final int compareTo(Actor<T> o) {
		return Integer.compare(getPriority().priority(), o.getPriority().priority());
	}

	/**
	 * Every actor object must have a topic which defines the job they do. And every
	 * actor must have a pointer to a router. by default we use the ActorCluster's
	 * router
	 * 
	 * @param topic
	 * @param router
	 * @param priority can be omitted.(appoint to Low, by default)
	 */
	protected Actor(ActorConfig<T> config) {
		adjustConfigurations(config);
		init();
	}

	private void adjustConfigurations(ActorConfig<T> config) {
		this.topic = config.getTopic();
		this.router = config.getRouter();
		this.divisionStrategy = config.getDivisionStrategy();
		this.priority = config.getPriority();
		this.waitList = config.getWaitList();
	}

	private void init() {
		this.cb = ControlBlockFactory.createCb(ActorType.WORKER);
		this.queueLock = new ReentrantLock(true);
		this.queue = new LinkedList<>();
		this.isNotified = false;

	}

	public Actor<?> getRootActor(String topic) {
		return this.router.getRootActor(topic);
	}

	public Cache getCache() {
		return getRouter().getDelayedCache();
	}

	@ThreadSafe
	public final List<String> getWaitListTopics() {
		List<String> result = new ArrayList<>();
		if (!CollectionUtils.isEmpty(this.waitList))
			this.waitList.stream().forEach((x) -> result.add(x.getTopic().getName()));
		return result;
	}

	/**
	 * This is a basic snapshoot of the current root-child family. <br>
	 * after calling this method the active node count both might be increased or
	 * decrased.
	 * 
	 * @return
	 */
	@ThreadSafe
	public final int getActiveNodeCount() {
		int result = 0;
		Actor<?> iter = this;
		while (iter != null) {
			if (Status.ACTIVE.equals(iter.cb.getStatus()))
				result++;
			iter = iter.childActor;
		}
		return result;
	}

	@Immutable
	@NotThreadSafe
	public final int getQueueSize() {
		return getQueue().size();
	}

	@Immutable
	@NotThreadSafe
	public final boolean isQueueEmpty() {
		return getQueue().isEmpty();
	}

	@Immutable
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	public final int getQueueSizeLocked() {
		queueLock.lock();
		try {
			return getQueue().size();
		} finally {
			queueLock.unlock();
		}

	}

	@Immutable
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	public final boolean isQueueEmptyLocked() {
		queueLock.lock();
		try {
			return getQueue().isEmpty();
		} finally {
			queueLock.unlock();
		}

	}

	/**
	 * Adds a message to the actor's queue according to loadingStrategy.<br>
	 * it's good to use to inititilize an actor that has some data to process.<br>
	 * <b>Data race is possible!</b>
	 * 
	 * @param message
	 */
	@Immutable
	public final void load(ActorMessage<T> message) {
		if (divisionStrategy.isConditionValid(this)) {
			divisionStrategy.executeLoadingStrategy(this, Arrays.asList(message));
		} else {
			queue.add(message);
		}
	}

	/**
	 * Adds a message to the actor's queue according to loadingStrategy.<br>
	 * it's good to use to inititilize an actor that has some data to process.<br>
	 * <b>Data race is possible!</b>
	 * 
	 * @param messageList
	 */

	@Immutable
	public final void loadAll(List<ActorMessage<T>> messageList) {
		if (divisionStrategy.isConditionValid(this)) {
			divisionStrategy.executeLoadingStrategy(this, messageList);
		} else {
			messageList.stream().forEach(x -> {
				if (divisionStrategy.isConditionValid(this)) {
					divisionStrategy.executeLoadingStrategy(this, Arrays.asList(x));
				} else
					queue.add(x);
			});
		}
	}

	/**
	 * Send an execution request right after calling loadAll(messageList).
	 * 
	 * @param messageList
	 */
	@Immutable
	public final void sendAll(List<ActorMessage<T>> messageList) {
		loadAll(messageList);
		sendExecutionRequest();
	}

	/**
	 * Adds a message to the actor's queue and then notifies the router uses locking
	 * mechanishm in order to avoid data race!
	 * 
	 * @param message
	 */
	@Immutable
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	public final void sendByLocking(ActorMessage<T> message) {
		queueLock.lock();
		try {
			if (divisionStrategy.isConditionValid(this)) {
				divisionStrategy.executeSendingStrategy(this, Arrays.asList(message));
			} else {
				queue.add(message);
				sendExecutionRequest();
			}
		} finally {
			queueLock.unlock();
		}
	}

	/**
	 * Adds list of messages to the actor's queue and then notifies the router uses
	 * locking mechanishm in order to avoid data race!
	 * 
	 * @param messageList
	 */
	@Immutable
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	public final void sendAllByLocking(List<ActorMessage<T>> messageList) {
		queueLock.lock();
		try {
			if (divisionStrategy.isConditionValid(this)) {
				divisionStrategy.executeSendingStrategy(this, messageList);
			} else {
				messageList.stream().forEach(x -> {
					if (divisionStrategy.isConditionValid(this)) {
						divisionStrategy.executeSendingStrategy(this, Arrays.asList(x));
					} else
						queue.add(x);
				});
				sendExecutionRequest();
			}

		} finally {
			queueLock.unlock();
		}
	}

	/**
	 * Notify cluster for execution only if it's not currently executed!
	 */
	private void sendExecutionRequest() {
		if (!this.isNotified && this.cb.getStatus().equals(Status.PASSIVE)) {
			this.router.executeNode(this);
			this.isNotified = true;
		}
	}

	/**
	 * send an execution request for both itself and it's children nodes.
	 */
	public void executeNodeStack() {
		Actor<?> temp = this;
		while (temp != null) {
			temp.sendExecutionRequest();
			temp = temp.getChildActor();
		}
	}

	/**
	 * 
	 * Returns a message object from the queue if exists or else returns and empty
	 * message obj.
	 * 
	 * @return
	 */
	@Immutable
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	private final ActorMessage<T> deq() {
		queueLock.lock();
		try {
			if (this.queue.isEmpty())
				return new ActorMessage<>(null);
			return queue.remove(0);
		} finally {
			queueLock.unlock();
		}
	}

	/**
	 * Appoints the given Actor node as childActor and returns it.
	 * 
	 * @return {@code Actor}
	 */
	@Immutable
	private final Actor<T> initChildActor(Actor<T> node) {
		this.router.incrementActorCount(node.getTopic());
		node.getCb().setIsRoot(false);
		node.getCb().setStatus(Status.PASSIVE);
		childActor = node;
		return node;
	}

	/**
	 * Returns an childActor if exists or else creates one and returns.
	 * 
	 * @return
	 */
	@Immutable
	public final Actor<T> fetchChildActor() {
		if (childActor != null)
			return childActor;
		return initChildActor(generateChildActor());
	}

	/**
	 * 
	 * Returns the waiting queue values and appoint the actor status to
	 * {@code Status.PASSIVE}
	 * 
	 */
	public List<ActorMessage<T>> terminateActor() {
		Thread.currentThread().interrupt();
		this.cb.setStatus(Status.PASSIVE);
		if (childActor != null) {
			queue.addAll(childActor.terminateActor());
		}
		List<ActorMessage<T>> response = queue;
		queue = new LinkedList<>();
		return response;
	}

	public Map<String, List<?>> terminateNodeStack() {
		return router.terminateTopic(topic);
	}

	/**
	 * Once the node is sent to threadPool this method is called.<br>
	 * you might want to override this method for advanced computations.
	 */
	@Override
	public Object call() throws Exception {
		try {
			router.waitForTermination(getWaitListTopics(), false);
			while (isProcessingAvailable()) {
				operate(deq());
			}
			if (Thread.currentThread().interrupted()) {
				System.out.println("interrupted!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.cb.setStatus(Status.PASSIVE);
		this.isNotified = false;
		return true;
	}

	/**
	 * This method defines that in what conditions actor continue to process waiting
	 * messages.
	 * 
	 * @return
	 */
	private boolean isProcessingAvailable() {
		return !getQueue().isEmpty() && Status.ACTIVE.equals(cb.getStatus()) && !Thread.currentThread().isInterrupted();
	}

	/**
	 * This method is the main objective that the node fulfill. <br>
	 * once the actor is executing by a thread of the threadPool, we'll send waiting
	 * messages to this method to be operated.
	 */
	public abstract void operate(ActorMessage<T> msg);

	/**
	 * This method must be implemented for creations of child actors that's used by
	 * the {@code divisionStrategy}.
	 * 
	 * @return
	 */
	public abstract Actor<T> generateChildActor();
}
