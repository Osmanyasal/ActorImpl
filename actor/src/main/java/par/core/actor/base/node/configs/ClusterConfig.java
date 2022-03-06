package par.core.actor.base.node.configs;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;
import par.core.actor.factories.ExecutorFactory.ThreadPoolTypes;

@Data
@Accessors(chain = true)
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
