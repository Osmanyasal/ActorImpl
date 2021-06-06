package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) throws Exception {

		ActorCluster cluster = new ActorCluster(new ClusterConfig().setName("main").setThreadCount(8));
		ActorNode<String> node1 = new ActorNode<>("osman", cluster.getRouter());

		cluster.addRootActor(node1);

		List<ActorMessage<String>> temp = new ArrayList<>();
		int i = 0;
		while (i < 10_000) {
			temp.add(new ActorMessage<String>().setMessage("this is root " + i));
			i++;
		}
		node1.sendAll(temp);
		Thread.sleep(1000);
		int pp = 0;
		while ((pp = cluster.getRemainingJobs("osman")) > 0) {
			Thread.sleep(50);
			System.err.println(pp);
		}
		System.out.println("program end!!");
		System.out.println("total actor :" + ActorNode.TOTAL_CREATION);
	}
}
