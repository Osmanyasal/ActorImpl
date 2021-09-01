package philosophers.arge.actor;

import org.apache.logging.log4j.LogManager;

import philosophers.arge.actor.ControlBlock.Status;

public final class ControlBlockFactory {
	private static final String NOT_FOUND = "Actor Type is not valid";

	private ControlBlockFactory() {

	}

	public static ControlBlock createCb(ActorType type) {

		switch (type) {
		case ROUTER:
			return new ControlBlock(ActorType.ROUTER, Status.ACTIVE, true);
		case CLUSTER:
			return new ControlBlock(ActorType.CLUSTER, Status.ACTIVE, true);
		case GATWEWAY:
			return new ControlBlock(ActorType.GATWEWAY, Status.ACTIVE, true);
		case WORKER:
			return new ControlBlock(ActorType.WORKER, Status.PASSIVE, true);
		default:
			LogManager.getLogger(ControlBlockFactory.class).info(NOT_FOUND);
			return null;
		}
	}
}
