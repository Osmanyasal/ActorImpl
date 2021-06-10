package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.List;

import philosophers.arge.actor.ClusterConfig.TerminationTime;

public class Main {
	public static void main(String[] args) throws Exception {

		ActorCluster cluster = new ActorCluster(getClusterConfig());
		cluster.addRootActor(new ActorNode<Integer>("topic", cluster.getRouter()));

	}

	public static ClusterConfig getClusterConfig() {
		ClusterConfig config = new ClusterConfig();
		config.setName("numericCluster");
		config.setThreadCount(Runtime.getRuntime().availableProcessors());
		config.setTerminationTime(TerminationTime.WHEN_EXECUTION_FINISHED);
		return config;
	}
}
