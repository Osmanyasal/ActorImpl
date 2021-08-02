package philosophers.arge.actor.terminators;

import java.util.List;
import java.util.Map;

public interface ClusterTerminator extends Terminable {
	/**
	 * returns remaining tasks on every nodes.
	 * 
	 * @param isPermenent
	 * @param showInfo
	 * @return
	 */
	Map<String, List<?>> terminateCluster(boolean isPermenent, boolean showInfo);
}
