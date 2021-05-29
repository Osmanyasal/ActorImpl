package philosophers.arge.actor;

public class ActorNode<TMessage> extends Actor<TMessage> {

	public ActorNode(String topic, RouterNode<RouterMessage<Object>> routerNode) {
		super(Type.DEFAULT, routerNode);
		setTopic(topic);
	}

	@Override
	public void operate() {
		System.out.println("this is --> "+getTopic());
		long length = getQueueSize();
		for (long i = 0; i < length; i++) {
			System.out.println(deq());
		}
	}
}