package philosophers.arge.actor;

import java.util.List;

public interface Terminable<TMessage> {
	List<TMessage> terminate();
}
