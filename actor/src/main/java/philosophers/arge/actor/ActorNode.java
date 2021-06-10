package philosophers.arge.actor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ActorNode<TMessage> extends Actor<TMessage> {

	private final int TRESHOLD_VALUE = 1_000;
	@Getter
	@Setter
	private int total = 0;

	public ActorNode(String topic, RouterNode<Object> routerNode) {
		super(topic, routerNode, null);
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
			total += (Integer) deq().getMessage();

		}
		System.out.println(getCb().getId().substring(0, 6) + " :" + total);
	}
}