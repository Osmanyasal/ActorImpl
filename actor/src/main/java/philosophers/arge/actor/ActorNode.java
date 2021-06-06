package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.List;

public class ActorNode<TMessage> extends Actor<TMessage> {

	private final int TRESHOLD_VALUE = 5;

	public ActorNode(String topic, RouterNode<Object> routerNode) {
		super(topic, routerNode, null);
		System.out.println(getCb().getId().substring(0, 6) + " created!!");
	}

	@Override
	public void operate() {
		long length = getQueueSize();
		if (length > TRESHOLD_VALUE) {
			List<ActorMessage<TMessage>> msgList = new ArrayList<>();
			while (getQueueSize() > TRESHOLD_VALUE)
				msgList.add(deq());
			generateActor().sendAll(msgList);
		}
		length = getQueueSize();
		for (long i = 0; i < length; i++) {
			System.out.println(getCb().getId().substring(0, 6) + " :" + deq());
		}
	}
}