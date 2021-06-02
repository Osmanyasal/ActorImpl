package philosophers.arge.actor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ControlBlock.Status;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class Actor<TMessage> extends ActorMessage<TMessage> implements Callable<Object>, Terminable {

	@Setter(value = AccessLevel.PRIVATE)
	private String topic;

	@Setter(value = AccessLevel.PRIVATE)
	private ControlBlock cb;

	@Setter(value = AccessLevel.PRIVATE)
	private List<ActorMessage<TMessage>> queue;

	@Setter(value = AccessLevel.PRIVATE)
	private long queueSize;

	@Setter(value = AccessLevel.PRIVATE)
	private RouterNode<Object> router;

	/**
	 * Every actor might have a child actor.
	 */
	@Setter(value = AccessLevel.PRIVATE)
	private Actor<TMessage> childActor;

	/**
	 * Every actor object must have a topic which defines the job they do. And every
	 * actor must have a pointer to a router. by default we use the ActorCluster's
	 * router
	 * 
	 * @param topic
	 * @param router
	 */
	protected Actor(String topic, RouterNode<Object> router) {
		this.router = router;
		this.topic = topic;

		this.cb = new ControlBlock(ActorType.WORKER, Status.PASSIVE, false);
		this.queue = new LinkedList<>();
		this.queueSize = 0;
	}

	/**
	 * adds a message to the actor's queue and then notifies the router
	 * 
	 * @param message
	 */
	public final void send(ActorMessage<TMessage> message) {
		queue.add(message);
		queueSize++;
		notifyRouter();
	}

	/**
	 * adds list of messages to the actor's queue and then notifies the router
	 * 
	 * @param messageList
	 */
	public final void sendAll(List<ActorMessage<TMessage>> messageList) {
		messageList.stream().forEach(x -> {
			queue.add(x);
			queueSize++;
		});
		notifyRouter();
	}

	/**
	 * when the node receives a message notifies the router to check it out via this
	 * method.
	 * 
	 * if the node is passive router is going to send the note to the pool for
	 * execution else it's just going to ignore notification.
	 */
	private void notifyRouter() {
		if (getCb().getStatus().equals(Status.ACTIVE))
			router.send(new RouterMessage<Object>().setMessage(this));
	}

	/**
	 * Returns an message object from queue if exists or else returns and empty
	 * message obj.
	 * 
	 * @return
	 */
	public final ActorMessage<TMessage> deq() {
		if (queueSize == 0)
			return new ActorMessage<>();
		queueSize--;
		return queue.remove(0);
	}

	/**
	 * Creates an child empty actor with the same settings like it's creator and
	 * returns it. It's used for data parallelism
	 * 
	 * @return {@code Actor}
	 */
	public final Actor<TMessage> generateActor() {
		Actor<TMessage> node = new ActorNode<>(getTopic(), getRouter());
		node.getCb().setIsRoot(false);
		childActor = node;
		return node;
	}

	/**
	 * Sets the actor's status passive clears waiting queue remove router connection
	 * kill's the children (whatt? T-T) remove the child connection.
	 * 
	 */
	public boolean terminate() {
		try {
			getCb().setStatus(Status.PASSIVE);
			queue.clear();
			queueSize = 0;
			router = null;
			if (childActor != null) {
				childActor.terminate();
				childActor = null;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * once the node is sent to threadPool this method is called you might want to
	 * override this method for advanced computations.
	 */
	@Override
	public Object call() throws Exception {
		operate();
		return true;
	}

	/**
	 * This method is the main objective that the node fulfil. Once you create an
	 * ActorNode you must override this method.
	 */
	public abstract void operate();
}
