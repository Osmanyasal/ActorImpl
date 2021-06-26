package philosophers.arge.actor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class Actor<TMessage> extends ActorMessage<TMessage>
		implements Callable<Object>, ActorTerminator<TMessage> {

	@Setter(value = AccessLevel.PRIVATE)
	private String topic;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private List<ActorMessage<TMessage>> queue;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	private RouterNode router;

	/**
	 * Every actor might have only one child actor.
	 */
	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	private Actor<TMessage> childActor;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private boolean isNotified;

	@Exclude
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Lock queueLock;

	private DivisionStrategy<TMessage> divisionStrategy;

	/**
	 * Every actor object must have a topic which defines the job they do. And every
	 * actor must have a pointer to a router. by default we use the ActorCluster's
	 * router
	 * 
	 * @param topic
	 * @param router
	 * @param priority can be omitted.(appoint to Low, by default)
	 */
	protected Actor(ActorConfig<TMessage> config) {
		adjustConfigurations(config);
		init();
	}

	private void adjustConfigurations(ActorConfig<TMessage> config) {
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

	/**
	 * Adds a message to the actor's queue according to loadingStrategy.<br>
	 * it's good to use to inititilize an actor that has some data to process.<br>
	 * <b>Data race is possible!</b>
	 * 
	 * @param message
	 */
	public final void load(ActorMessage<TMessage> message) {
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
	public final void loadAll(List<ActorMessage<TMessage>> messageList) {
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
	public final void sendAll(List<ActorMessage<TMessage>> messageList) {
		loadAll(messageList);
		sendExecutionRequest();
	}

	/**
	 * adds a message to the actor's queue and then notifies the router uses locking
	 * mechanishm in order to avoid data race!
	 * 
	 * @param message
	 */
	public final void sendByLocking(ActorMessage<TMessage> message) {
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
	 * adds list of messages to the actor's queue and then notifies the router uses
	 * locking mechanishm in order to avoid data race!
	 * 
	 * @param messageList
	 */
	public final void sendAllByLocking(List<ActorMessage<TMessage>> messageList) {
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
		if (!this.isNotified && getCb().getStatus().equals(Status.PASSIVE)) {
			this.router.getCluster().executeNode(this);
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
	public final ActorMessage<TMessage> deq() {
		queueLock.lock();
		try {
			if (this.queue.isEmpty())
				return new ActorMessage<>();
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
	private final Actor<TMessage> initChildActor(Actor<TMessage> node) {
		this.router.incrementActorCount(node.getTopic());
		node.getCb().setIsRoot(false);
		node.getCb().setStatus(Status.PASSIVE);
		childActor = node;
		return node;
	}

	public final Actor<TMessage> fetchChildActor() {
		if (childActor != null)
			return childActor;
		return initChildActor(generateChildActor());
	}

	/**
	 * 
	 * Returns the waiting queue values and appoint the actor status to passsive!;
	 * 
	 */
	public List<ActorMessage<TMessage>> terminate() {
		this.cb.setStatus(Status.PASSIVE);
		if (childActor != null) {
			queue.addAll(childActor.terminate());
		}
		return queue;
	}

	/**
	 * once the node is sent to threadPool this method is called you might want to
	 * override this method for advanced computations.
	 */
	@Override
	public Object call() throws Exception {
		try {
			operate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// set status passive after execution is done!!
		this.cb.setStatus(Status.PASSIVE);
		this.isNotified = false;
		return true;
	}

	/**
	 * This method is the main objective that the node fulfil. Once you create an
	 * ActorNode you must override this method.
	 */
	public abstract void operate();

	public abstract Actor<TMessage> generateChildActor();
}
