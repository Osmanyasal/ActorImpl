package philosophers.arge.actor.cache;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import philosophers.arge.actor.annotations.ThreadSafe;
import philosophers.arge.actor.terminators.Terminable;
import philosophers.arge.actor.terminators.Terminate;

/**
 * We can easily store some calculated operations results in order to reduce
 * re-calculation time.
 * 
 * 
 * @author osmanyasal
 *
 */
public class DelayedCache implements Cache, Terminate {

	private final ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<>();
	private final DelayQueue<DelayedCacheObject> cleaningUpQueue = new DelayQueue<>();
	private Thread cleanerThread;

	public DelayedCache() {
		initCleanerThread();
	}

	/**
	 * deamon cleaner thread, clears the items of the cache that its time is exceed
	 */
	private void initCleanerThread() {
		this.cleanerThread = new Thread(() -> {
			DelayedCacheObject delayedCacheObject;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					delayedCacheObject = this.cleaningUpQueue.take();
					this.cache.remove(delayedCacheObject.getKey(), delayedCacheObject.getReference());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		this.cleanerThread.setDaemon(true);
		this.cleanerThread.start();
	}

	@Override
	@ThreadSafe
	public void add(String key, Object value, long periodInMillis) {
		if (key == null) {
			return;
		}
		if (value == null) {
			cache.remove(key);
		} else {
			long expiryTime = System.currentTimeMillis() + periodInMillis;
			SoftReference<Object> reference = new SoftReference<>(value);
			cache.put(key, reference);
			cleaningUpQueue.put(new DelayedCacheObject(key, reference, expiryTime));
		}
	}

	@Override
	@ThreadSafe
	public void remove(String key) {
		cache.remove(key);
	}

	@Override
	@ThreadSafe
	public Object get(String key) {
		return Optional.ofNullable(cache.get(key)).map(SoftReference::get).orElse(null);
	}

	@Override
	@ThreadSafe
	public void clear() {
		cache.clear();
	}

	@Override
	@ThreadSafe
	public long size() {
		return cache.size();
	}

	@Override
	public void terminate() {
		if (cleanerThread.isAlive())
			cleanerThread.interrupt();
		cleaningUpQueue.clear();
		cache.clear();
	}

	@AllArgsConstructor
	@EqualsAndHashCode
	private static class DelayedCacheObject implements Delayed {

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
}