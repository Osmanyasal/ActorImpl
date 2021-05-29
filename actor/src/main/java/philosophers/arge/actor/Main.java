package philosophers.arge.actor;

public class Main {
	public static void main(String[] args) throws Exception {
		ActorCluster cluster = new ActorCluster(new ClusterConfig().setThreadCount(4));
		ActorNode<String> node1 = new ActorNode<String>("default", cluster.getRouter());
		ActorNode<String> node2 = new ActorNode<String>("temp", cluster.getRouter());
		cluster.addRootActor(node1);
		cluster.addRootActor(node2);

		node1.send(new ActorMessage<String>().setMessage("this is root"));
		node2.getRouter().send(new RouterMessage<>().setMessage("node2 to node1 test").setTopic("default"));
		node1.getRouter().send(new RouterMessage<>().setMessage("node1 to node1 test").setTopic("temp"));
	}
}
