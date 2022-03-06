package par.core.actor.base;

public enum ActorPriority {
	MAX(0), HIGH(100), MEDIUM(200), LOW(300), VERY_LOW(400), DEFAULT(500);

	private int value;

	ActorPriority(int value) {
		this.value = value;
	}

	public int priority() {
		return this.value;
	}
}
