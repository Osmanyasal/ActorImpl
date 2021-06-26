package philosophers.arge.actor;

import java.util.List;
import java.util.Map;

public interface RouterTerminator extends Terminable {
	Map<String, List<?>> terminateRouter();
}
