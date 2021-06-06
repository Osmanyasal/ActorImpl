package philosophers.arge.actor;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClusterConfig {
	private String name;
	private int threadCount;

	public ClusterConfig() {
		name = UUID.randomUUID().toString();
		threadCount = Runtime.getRuntime().availableProcessors();
		System.out.println((String.format("clusterName :%s + threadCount : %s", name, threadCount)));
	}
}
