package philosophers.arge.actor;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ExecutorFactory.ThreadPoolTypes;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClusterConfig {
	public enum TerminationTime {
		WHEN_EXECUTION_IS_FINISHED, ON_DEMAND, NEVER,
	}

	private String name;
	private int threadCount;
	private ThreadPoolTypes poolType;
	private TerminationTime terminationTime;

	/**
	 * 
	 * @param poolType can be omitted by default creates
	 *                 {@code FixedSizedThreadPool} with default thread count
	 */
	public ClusterConfig(ThreadPoolTypes poolType) {
		this.name = UUID.randomUUID().toString();
		this.threadCount = Runtime.getRuntime().availableProcessors() * 2;
		this.terminationTime = TerminationTime.ON_DEMAND;
		this.poolType = poolType != null ? poolType : ThreadPoolTypes.FIXED_SIZED;
	}
}
