package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActorCluster {

	private String name;
	private RouterNode<RouterMessage<Object>> router;
	private Object gateway;
	private Type type;
	private ExecutorService pool;
	private List<Future<?>> futures;

	public ActorCluster(ClusterConfig config) {
		this.name = config.getName();
		pool = Executors.newFixedThreadPool(config.getThreadCount());
		futures = new ArrayList<>();
		type = Type.CLUSTER;
		router = new RouterNode<RouterMessage<Object>>(this);
		futures.add(pool.submit(router));
	}

	@SuppressWarnings("unchecked")
	public <TMessage> void addRootActor(ActorNode<TMessage> node) {
		router.addRootActor(node.getTopic(), (ActorNode<Object>) node);
	}

	public <TMessage> void executeNode(ActorNode<TMessage> node) {
		futures.add(pool.submit(node));
	}

	public void terminate() throws InterruptedException {
		Thread.sleep(5000);
		pool.shutdown();
	}
}
