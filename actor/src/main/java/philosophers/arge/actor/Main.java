package philosophers.arge.actor;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ExecutorFactory.ThreadPoolTypes;
import philosophers.arge.actor.configs.ActorConfig;
import philosophers.arge.actor.configs.ClusterConfig;
import philosophers.arge.actor.divisionstrategies.NumberBasedDivison;

public class Main {

	public static void main(String[] args) {
		ActorCluster cluster = new ActorCluster(new ClusterConfig(ThreadPoolTypes.FIXED_SIZED, false));
		ActorConfig<List<String>> config = new ActorConfig<>(new Topic("nodeCounter"), cluster.getRouter(),
				new NumberBasedDivison<List<String>>(5l), ActorPriority.DEFAULT, null);
		cluster.addRootActor(new CounterNode(config, "aa"));
		String json = cluster.toJson();
		System.out.println(json);

	}
}

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
class CounterNode extends Actor<List<String>> {

	private ActorConfig<List<String>> config;
	private String lookingFor;
	private long count;

	protected CounterNode(ActorConfig<List<String>> config, String lookingFor) {
		super(config);
		this.config = config;
		this.lookingFor = lookingFor;
		this.count = 0;
	}

	@Override
	public Actor<List<String>> generateChildActor() {
		return new CounterNode(this.config, getLookingFor());
	}

	@Override
	public void operate(ActorMessage<List<String>> message) {
		List<String> msg = message.getMessage();

		// process
		for (int i = 0; i < msg.size(); i++) {
			if (msg.get(i).contains(lookingFor))
				count++;
		}
	}

}
