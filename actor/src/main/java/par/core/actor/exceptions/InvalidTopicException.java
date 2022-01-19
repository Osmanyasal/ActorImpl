package par.core.actor.exceptions;

public class InvalidTopicException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTopicException(String topic) {
		super(String.format("The [%s] topic is not registered", topic));
	}
}
