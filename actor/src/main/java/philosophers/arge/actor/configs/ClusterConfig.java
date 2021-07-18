package philosophers.arge.actor.configs;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import philosophers.arge.actor.ExecutorFactory.ThreadPoolTypes;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClusterConfig {

	private String name;
	private int threadCount;
	private ThreadPoolTypes poolType;
	private boolean isDeamon;

	/**
	 * 
	 * @param poolType can be omitted by default creates
	 *                 {@code FixedSizedThreadPool} with default thread count
	 */
	public ClusterConfig(ThreadPoolTypes poolType, boolean isDeamon) {
		this.name = UUID.randomUUID().toString();
		this.threadCount = Runtime.getRuntime().availableProcessors() * 2;
		this.poolType = poolType != null ? poolType : ThreadPoolTypes.FIXED_SIZED;
		this.isDeamon = isDeamon;
	}
}
