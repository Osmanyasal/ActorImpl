package philosophers.arge.actor;

public class Main {
	public static void main(String[] args) throws Exception {

		ActorCluster cluster = new ActorCluster(new ClusterConfig().setName("main").setThreadCount(8));
		ActorNode<String> node1 = new ActorNode<>("osman", cluster.getRouter());

		cluster.addRootActor(node1);

		int i = 0;
		while (i < 10) {
			node1.send(new ActorMessage<String>().setMessage("this is root " + i));
			i++;
		}
		Thread.sleep(1000);
		for (String key : cluster.getFutures().keySet()) {
			System.out.println("key :: " + key + cluster.getFutures().get(key));
		}
	}
}
