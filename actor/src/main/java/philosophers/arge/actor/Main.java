package philosophers.arge.actor;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import philosophers.arge.actor.configs.ActorConfig;

public class Main {

	public static void main(String[] args) throws InterruptedException {
//		ActorCluster cluster = new ActorCluster(new ClusterConfig(ThreadPoolTypes.FIXED_SIZED, false));
//		ActorConfig<List<String>> config = new ActorConfig<>(new Topic("nodeCounter"), cluster.getRouter(),
//				new NumberBasedDivison<List<String>>(5l), ActorPriority.DEFAULT, null);
//		cluster.addRootActor(new CounterNode(config, "aa"));
//		String json = cluster.toJson();
//		System.out.println(json);

		DelayQueue<DelayedCacheObject> cleaningUpQueue = new DelayQueue<>();

		cleaningUpQueue.add(new DelayedCacheObject("1000", null, System.currentTimeMillis() + 20000));
		cleaningUpQueue.add(new DelayedCacheObject("10", null, System.currentTimeMillis() + 2000));
		cleaningUpQueue.add(new DelayedCacheObject("1", null, System.currentTimeMillis() + 2));

		System.out.println(cleaningUpQueue.take().toString());
		System.out.println(cleaningUpQueue.take().toString());
		System.out.println(cleaningUpQueue.take().toString());
		System.out.println("program done!!");
	}
}

@ToString
@AllArgsConstructor
@EqualsAndHashCode
class DelayedCacheObject implements Delayed {

	@Getter
	private final String key;
	@Getter
	private final SoftReference<Object> reference;
	private final long expiryTime;

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return Long.compare(expiryTime, ((DelayedCacheObject) o).expiryTime);
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
