package par.core.actor.cache;

import java.util.concurrent.TimeUnit;

public interface Cache {

	void add(String key, Object value, long time, TimeUnit timeUnit);

	void remove(String key);

	Object get(String key);

	void clear();

	long size();

}