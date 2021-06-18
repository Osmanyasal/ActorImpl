package philosophers.arge.actor;

import java.util.PriorityQueue;

public class Main {

	public static void main(String[] args) throws Exception {
		clusterExecution();
	}

	private static void clusterExecution() throws InterruptedException {
		// init cluster
		ActorCluster cluster = new ActorCluster(new ClusterConfig());
		FirstNode<String> node1 = new FirstNode<>("first node", cluster.getRouter(), null, new NoDivision<>());
		SecondNode<String> node2 = new SecondNode<>("second node", cluster.getRouter(), null, new NoDivision<>());
		cluster.addRootActor(node1);
		cluster.addRootActor(node2);

		// load data
		for (int i = 0; i < 100; i++)
			node1.load(new ActorMessage<String>().setMessage("msg[" + i + "]"));

		node1.sendExecutionRequest();
		cluster.waitForTermination();
		cluster.terminateThreadPool();
	}
}

class FirstNode<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;

	protected FirstNode(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {
			String msg = (String) deq().getMessage();
			System.out.println("first(" + msg + ")");

			@SuppressWarnings("unchecked")
			Actor<String> nextActor = (Actor<String>) getRouter().getRootActor("second node");
			nextActor.sendByLocking(new ActorMessage<String>().setMessage(msg));
		}
	}

	@Override
	public Actor<T> generateChildActor() {
		return new FirstNode<>(getTopic(), getRouter(), getPriority(), strategy);
	}
}

class SecondNode<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;

	protected SecondNode(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {
			String msg = (String) deq().getMessage();
			System.out.println("second(" + msg + ")");
		}
	}

	@Override
	public Actor<T> generateChildActor() {
		return new SecondNode<>(getTopic(), getRouter(), getPriority(), strategy);
	}
}
