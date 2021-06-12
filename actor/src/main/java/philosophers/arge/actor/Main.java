package philosophers.arge.actor;

public class Main {
	public static void main(String[] args) throws Exception {

		final int LIMIT = 50_000;

		serialExecution(LIMIT);

		System.out.println("====");

		clusterExecution(LIMIT);

	}

	private static void serialExecution(final int LIMIT) throws InterruptedException {
		System.out.println("Serial Execution : ");
		long start = System.currentTimeMillis();
		for (int i = 0; i < LIMIT; i++) {
			Thread.sleep(10);
		}
		long end = System.currentTimeMillis();
		System.out.println("timer : " + (end - start));
	}

	private static void clusterExecution(final int LIMIT) throws InterruptedException {
		System.out.println("Cluster Execution : ");

		// init cluster
		ActorCluster cluster = new ActorCluster(new ClusterConfig());
		ActorNode<String> node1 = new ActorNode<>("typing", cluster.getRouter(), null,
				new NumberBasedDivison<>(1_000l));
		cluster.addRootActor(node1);

		// load data
		for (int i = 0; i < LIMIT; i++)
			node1.load(new ActorMessage<String>().setMessage("" + i));

		// execution
		long start = System.currentTimeMillis();
		node1.sendExecutionRequest();
		cluster.waitTermination();
		long end = System.currentTimeMillis();

		System.out.println("timer : " + (end - start));
		cluster.getPool().shutdown();
	}
}

class ActorNode<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;

	protected ActorNode(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {
			try {
				deq();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public Actor<T> generateChildActor() {
		return new ActorNode<T>(getTopic(), getRouter(), getPriority(), strategy);
	}

}