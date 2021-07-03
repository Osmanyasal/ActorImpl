package philosophers.arge.actor;

import java.util.List;

import philosophers.arge.actor.ClusterConfig.TerminationTime;
import philosophers.arge.actor.ControlBlock.Status;
import philosophers.arge.actor.divisionstrategies.NumberBasedDivison;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		actorExample();
	}

	public static void actorExample() throws InterruptedException {
		ActorCluster cluster = new ActorCluster(
				new ClusterConfig().setName("main").setTerminationTime(TerminationTime.ON_DEMAND).setThreadCount(8));

		Temp actor = new Temp(new ActorConfig<String>("temp", cluster.getRouter(), new NumberBasedDivison<String>(1l)));

		for (int i = 0; i < 10; i++)
			actor.load(new ActorMessage<String>("empty : " + i));
		actor.executeNodeStack();

		cluster.terminateCluster();
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
		System.out.println(msg.getMessage());
	}

	@Override
	public Actor<String> generateChildActor() {
		return new Temp(config);
	}

}
