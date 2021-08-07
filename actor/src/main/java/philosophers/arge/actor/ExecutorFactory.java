package philosophers.arge.actor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ExecutorFactory {

	public enum ThreadPoolTypes {
		FIXED_SIZED, CACHED_SIZED, PRIORITIZED
	}

	private ExecutorFactory() {
	}

	public static ExecutorService getExecutor(ThreadPoolTypes poolType, int size) {
		switch (poolType) {
		case CACHED_SIZED:
			return Executors.newCachedThreadPool();
		case PRIORITIZED:
			return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
		case FIXED_SIZED:
		default:
			return Executors.newFixedThreadPool(size);
		}
	}
}
