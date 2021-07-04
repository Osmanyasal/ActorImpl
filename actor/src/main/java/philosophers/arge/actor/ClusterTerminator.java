package philosophers.arge.actor;

import java.util.List;
import java.util.Map;

public interface ClusterTerminator extends Terminable {
	Map<String, List<?>> terminateCluster(boolean isPermenent);
}
