package philosophers.arge.actor;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

public class Main {

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 5; i++) {
			float cx = clusterExecution();
			System.gc();
			float sx = serialExecution();
			System.gc();

			System.out.println(
					String.format("Time(Serial) : %s\nTime(Parallel) : %s\nSpeedUp : %sx", sx, cx, (sx / (float) cx)));
		}
	}

	private static long serialExecution() throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1_000; i++) {
			new Image();
			Thread.sleep(38);
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	private static long clusterExecution() throws InterruptedException {

		// init cluster
		ActorCluster cluster = new ActorCluster(new ClusterConfig());

		// node defining
		Enhancing<Image> enhancing = new Enhancing<>("enhancing", cluster.getRouter(), null,
				new NumberBasedDivison<>(17l));
		cluster.addRootActor(enhancing);

		Filtering<Image> filtering = new Filtering<>("filtering", cluster.getRouter(), null,
				new NumberBasedDivison<>(34l));
		cluster.addRootActor(filtering);

		Decompression<Image> decompression = new Decompression<>("decompression", cluster.getRouter(), null,
				new NumberBasedDivison<>(17l));
		cluster.addRootActor(decompression);


		long first = System.currentTimeMillis();
		
		// loading data 
		for (int i = 0; i < 1_000; i++)
			decompression.load(new ActorMessage<Image>().setMessage(new Image()));

		decompression.sendExecutionRequest();
		
		cluster.waitForTermination();
		long second = System.currentTimeMillis();

		// terminate cluster
		cluster.terminateThreadPool();
		return (second - first);
	}
}

@Data
@Accessors(chain = true)
class Image {
	private String id;
	private int[][] image;

	public Image() {
		id = UUID.randomUUID().toString();
		image = new int[480][540]; // -> 259KB
	}
}

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
class Decompression<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;

	private Actor<Image> nextStep;
	private Image image;

	@SuppressWarnings("unchecked")
	protected Decompression(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
		nextStep = (Actor<Image>) getRouter().getRootActor("filtering");
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {
			// getting next image
			image = (Image) deq().getMessage();

			// processing
			image = deCompress(image);

			// transmitting to next step
			nextStep.sendByLocking(new ActorMessage<Image>().setMessage(image));
		}
	}

	private Image deCompress(Image image) {
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return image;
	}

	@Override
	public Actor<T> generateChildActor() {
		return new Decompression<>(getTopic(), getRouter(), getPriority(), strategy);
	}
}

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
class Filtering<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;
	private Actor<Image> nextStep;
	private Image image;

	@SuppressWarnings("unchecked")
	protected Filtering(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
		nextStep = (Actor<Image>) getRouter().getRootActor("enhancing");
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {
			// getting next image
			image = (Image) deq().getMessage();

			// processing
			image = filter(image);

			// transmitting to next step
			nextStep.sendByLocking(new ActorMessage<Image>().setMessage(image));
		}
	}

	private Image filter(Image image) {
		try {
			Thread.sleep(8);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return image;
	}

	@Override
	public Actor<T> generateChildActor() {
		return new Filtering<>(getTopic(), getRouter(), getPriority(), strategy);
	}
}

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
class Enhancing<T> extends Actor<T> {
	private DivisionStrategy<T> strategy;
	private Image image;

	protected Enhancing(String topic, RouterNode router, ActorPriority priority, DivisionStrategy<T> strategy) {
		super(topic, router, priority, strategy);
		this.strategy = strategy;
	}

	@Override
	public void operate() {
		while (!getQueue().isEmpty()) {

			// getting next image
			image = (Image) deq().getMessage();

			// processing
			image = enhance(image);

			// transmitting to next step

			// System.out.println("image[" + image.getId().substring(0, 6) + "] : ready");
		}
	}

	private Image enhance(Image image) {
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return image;
	}

	@Override
	public Actor<T> generateChildActor() {
		return new Enhancing<>(getTopic(), getRouter(), getPriority(), strategy);
	}
}
