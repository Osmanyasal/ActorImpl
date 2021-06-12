package philosophers.arge.actor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		ActorNode<String> node1 = new ActorNode<>("typing", null, null, new NumberBasedDivison<>(3l));
		node1.send(new ActorMessage<String>().setMessage("1-this is root"));
		node1.send(new ActorMessage<String>().setMessage("2-this is root"));
		node1.send(new ActorMessage<String>().setMessage("3-this is root"));
		node1.send(new ActorMessage<String>().setMessage("4-this is root"));
		node1.send(new ActorMessage<String>().setMessage("5-this is root"));
		node1.send(new ActorMessage<String>().setMessage("6-this is root"));

		pool.submit(node1);
		pool.submit(node1.fetchChildActor());
		pool.shutdown();
	}
}

class ActorNode<TMessage> extends Actor<TMessage> {
	private DivisionStrategy<TMessage> strategy;

	protected ActorNode(String topic, RouterNode<Object> router, ActorPriority priority,
			DivisionStrategy<TMessage> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
	}

	@Override
	public void operate() {
		while (getQueue().size() > 0)
			System.out.println(deq().getMessage());

	}

	@Override
	public Actor<TMessage> generateChildActor() {
		System.out.println("child actor generated!");
		return new ActorNode<TMessage>(getTopic(), getRouter(), getPriority(), strategy);
	}

}