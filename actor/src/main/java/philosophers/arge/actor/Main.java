package philosophers.arge.actor;

import philosophers.arge.actor.ExecutorFactory.ThreadPoolTypes;
import philosophers.arge.actor.configs.ActorConfig;
import philosophers.arge.actor.configs.ClusterConfig;
import philosophers.arge.actor.divisionstrategies.NumberBasedDivison;

public class Main {

	public static void main(String[] args) throws InterruptedException {
	}

	public static void actorExample() throws InterruptedException {
		ActorCluster cluster = new ActorCluster(new ClusterConfig(ThreadPoolTypes.FIXED_SIZED, false));
		Temp temp = new Temp(new ActorConfig<String>(new Topic(Temp.class.getSimpleName()), cluster.getRouter(),
				new NumberBasedDivison<String>(20l), ActorPriority.DEFAULT, null));
		cluster.addRootActor(temp);

		for (int i = 1; i < 1000; i++) {
			temp.load(new ActorMessage<>("" + i));
		}
		temp.executeNodeStack();
		try {
			cluster.waitForTermination(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Temp extends Actor<String> {
	private ActorConfig<String> config;

	protected Temp(ActorConfig<String> config) {
		super(config);
		this.config = config;
	}

	@Override
	public void operate(ActorMessage<String> msg) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("message : " + msg.getMessage());
		if (msg.getMessage().equals("600")) {
			System.out.println("600 was found in -> " + getCb().getId().substring(0, 6) + " terminating all nodes!!");
			terminateNodeStack();
		}
	}

	@Override
	public Actor<String> generateChildActor() {
		return new Temp(config);
	}

}