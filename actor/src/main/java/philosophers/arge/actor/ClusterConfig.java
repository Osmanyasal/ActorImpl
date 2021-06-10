package philosophers.arge.actor;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClusterConfig {
	public enum TerminationTime {
		WHEN_EXECUTION_FINISHED, ON_DEMAND, NEVER,
	}

	private String name;
	private int threadCount;
	private TerminationTime terminationTime;

	public ClusterConfig() {
		name = UUID.randomUUID().toString();
		threadCount = Runtime.getRuntime().availableProcessors();
		terminationTime = TerminationTime.NEVER;

		System.out.println(this.toString());
	}
}
