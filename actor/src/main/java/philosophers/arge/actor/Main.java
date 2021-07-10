package philosophers.arge.actor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.experimental.Accessors;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		actorExample();
	}

	public static void actorExample() throws InterruptedException {
		ExecutorService pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
		pool.submit(new Temp(ActorPriority.DEFAULT));
		pool.submit(new Temp(ActorPriority.LOW));
		pool.submit(new Temp(ActorPriority.MEDIUM));
		pool.submit(new Temp(ActorPriority.HIGH));
		pool.submit(new Temp(ActorPriority.MAX));
	}
}

@Data
@Accessors(chain = true)
class Temp implements Comparable<Temp>, Runnable {
	ActorPriority priority;

	public Temp(ActorPriority priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Temp o) {
		return Integer.compare(getPriority().priority(), o.getPriority().priority());
	}

	@Override
	public void run() {
		System.out.println("this is :" + priority.priority());
	}

}