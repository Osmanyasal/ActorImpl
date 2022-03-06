package par.core.actor.factories;

import org.apache.logging.log4j.LogManager;

import par.core.actor.base.ControlBlock;
import par.core.actor.base.ControlBlock.Status;
import par.core.actor.base.Type;

public final class ControlBlockFactory {
	private static final String NOT_FOUND = "Actor Type is not valid";

	private ControlBlockFactory() {

	}

	public static ControlBlock createCb(Type type) {
		switch (type) {
		case ROUTER:
			return new ControlBlock(Type.ROUTER, Status.ACTIVE, true);
		case CLUSTER:
			return new ControlBlock(Type.CLUSTER, Status.ACTIVE, true);
		case GATWEWAY:
			return new ControlBlock(Type.GATWEWAY, Status.ACTIVE, true);
		case WORKER:
			return new ControlBlock(Type.WORKER, Status.PASSIVE, true);
		default:
			LogManager.getLogger(ControlBlockFactory.class).info(NOT_FOUND);
			return null;
		}
	}
}
