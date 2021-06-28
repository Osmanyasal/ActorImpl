package philosophers.arge.actor;

import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws Exception {
		ActorCluster cluster = new ActorCluster(new ClusterConfig());
		TempActor actor = new TempActor(
				new ActorConfig<Integer>("deneme", cluster.getRouter(), new NumberBasedDivison(1l)));
		cluster.addRootActor(actor);

		for (int i = 0; i < 1_500; i++)
			actor.load(new ActorMessage<>(i));

		actor.executeNodeStack();

		 
	}

	public static int[][] multiplyMatrix(int[][] a, int[][] b) {
		int size = a.length;
		int product[][] = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				product[i][j] = 0;
				for (int k = 0; k < size; k++) {
					product[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return product;
	}
}

class TempActor extends Actor<Integer> {
	private ActorConfig<Integer> config;

	protected TempActor(ActorConfig<Integer> config) {
		super(config);
		this.config = config;
	}

	@Override
	public void operate() {
		System.out.println("actor ops.");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public Actor<Integer> generateChildActor() {
		return new TempActor(config);
	}

}