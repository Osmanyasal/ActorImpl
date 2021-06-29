package philosophers.arge.actor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import philosophers.arge.actor.ControlBlock.Status;
import philosophers.arge.actor.annotations.GuardedBy;
import philosophers.arge.actor.annotations.NotThreadSafe;
import philosophers.arge.actor.annotations.ThreadSafe;
import philosophers.arge.actor.divisionstrategies.DivisionStrategy;

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
@NotThreadSafe
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class Actor<T> implements Callable<Object>, ActorTerminator<T> {

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private String topic;

	@Setter(value = AccessLevel.PRIVATE)
	private List<ActorMessage<T>> queue;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
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
	}

	private void init() {
		this.queueLock = new ReentrantLock(true);
		this.cb = new ControlBlock(ActorType.WORKER, Status.PASSIVE, true);
		this.queue = new LinkedList<>();
		this.isNotified = false;
	}

	public final int getActiveNodeCount() {
		int result = 0;
		Actor<?> iter = this;
		while (iter != null) {
			if (iter.cb.getStatus().equals(Status.ACTIVE))
				result++;
			iter = iter.childActor;
		}
		return result;
	}

	/**
	 * Adds a message to the actor's queue according to loadingStrategy.<br>
	 * it's good to use to inititilize an actor that has some data to process.<br>
	 * <b>Data race is possible!</b>
	 * 
	 * @param message
	 */
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
	 * send an execution request right after calling loadAll(messageList).
	 * 
	 * @param messageList
	 */
	public final void sendAll(List<ActorMessage<T>> messageList) {
		loadAll(messageList);
		sendExecutionRequest();
	}

	/**
	 * adds a message to the actor's queue and then notifies the router uses locking
	 * mechanishm in order to avoid data race!
	 * 
	 * @param message
	 */
	@ThreadSafe
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
	@ThreadSafe
	@GuardedBy(Actor.Fields.queueLock)
	public final ActorMessage<T> deq() {
		queueLock.lock();
		try {
			if (this.queue.isEmpty())
				return new ActorMessage<>(null);
			return queue.remove(0);
		} finally {
			queueLock.unlock();
		}
	}

	// oop violation can't use actorNode!!!
	/**
	 * appoint the given Actor node as childActor and returns it.
	 * 
	 * @return {@code Actor}
	 */
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
	public List<ActorMessage<T>> terminate() {
		this.cb.setStatus(Status.PASSIVE);
		if (childActor != null) {
			queue.addAll(childActor.terminate());
		}
		return queue;
	}

	/**
	 * Once the node is sent to threadPool this method is called.<br>
	 * you might want to override this method for advanced computations.
	 */
	@Override
	public Object call() throws Exception {
		try {
			operate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.cb.setStatus(Status.PASSIVE);
		this.isNotified = false;
		return true;
	}

	/**
	 * This method is the main objective that the node fulfil. Once you create an
	 * ActorNode you must override this method.
	 */
	public abstract void operate();

	/**
	 * This method must be implemented for creations of child actors that's used by
	 * the {@code divisionStrategy}.
	 * 
	 * @return
	 */
	public abstract Actor<T> generateChildActor();
}
