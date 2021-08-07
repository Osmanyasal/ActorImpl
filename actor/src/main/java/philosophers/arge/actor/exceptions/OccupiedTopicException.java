package philosophers.arge.actor.exceptions;

public class OccupiedTopicException extends RuntimeException {

	private static final String OCCUPIED_TOPIC = "This topic is already occupied!!";
	private static final long serialVersionUID = -9069569002788184325L;

	public OccupiedTopicException() {
		super(OCCUPIED_TOPIC);
	}
}