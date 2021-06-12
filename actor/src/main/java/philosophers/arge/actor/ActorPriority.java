package philosophers.arge.actor;

public enum ActorPriority {
	LOW(0), MEDIUM_LOW(2.5f), MEDIUM(5), MEDIUM_HIGH(7.5f), HIGH(10);

	private float range;

	ActorPriority(float range) {
		this.range = range;
	}

	public float getRange() {
		return range;
	}
}
