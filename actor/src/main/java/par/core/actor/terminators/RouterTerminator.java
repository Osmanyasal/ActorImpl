package par.core.actor.terminators;

import java.util.List;
import java.util.Map;

/**
 * Returns messageList per actor topic. <br>
 * ex output: <br>
 * { <br>
 * "node1" : [ActorMessage(msg = "msg1"),ActorMessage(msg = "msg2")], <br>
 * "node2" : [ActorMessage(msg = 5),ActorMessage(msg = 382)], <br>
 * "pool" : [Callable(...),Callable(...)], <br>
 * <br>
 * } <br>
 * @author osmanyasal
 *
 */
public interface RouterTerminator extends Terminable {
	Map<String, List<?>> terminateRouter();
}
