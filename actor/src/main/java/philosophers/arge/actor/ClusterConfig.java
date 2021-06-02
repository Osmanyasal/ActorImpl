package philosophers.arge.actor;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClusterConfig {
	private String name;
	private int threadCount;
	
}
