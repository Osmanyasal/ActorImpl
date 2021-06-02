package philosophers.arge.actor;

public class Main {
	public static void main(String[] args) throws Exception {

		ActorCluster cluster = new ActorCluster(new ClusterConfig().setName("main").setThreadCount(8));
		ActorCluster cluster2 = new ActorCluster(new ClusterConfig().setName("main2").setThreadCount(8));

		ActorNode<String> node1 = new ActorNode<>("default", cluster.getRouter());
		ActorNode<String> node2 = new ActorNode<>("temp", cluster.getRouter());
		// both adds node
		/**
		 * node' kayit islemi termination suresi boyunca yolculuk kontrol edilecek.
		 */
		int i = 0;
		while (i < 500) {
			node1.send(new ActorMessage<String>().setMessage("this is root "));
			node2.send(new ActorMessage<String>().setMessage("22this is root"));
			i++;
		}
		System.out.println("waiting status");
		Thread.sleep(2000);
		cluster.addRootActor(node1);
		cluster2.addRootActor(node2);
		System.out.println(cluster.terminate());
		System.out.println(cluster2.terminate());
		System.out.println("program end!!");
	}
}
