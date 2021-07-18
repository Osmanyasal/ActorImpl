package philosophers.arge.actor.divisionstrategies;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import philosophers.arge.actor.Actor;
import philosophers.arge.actor.ActorMessage;
import philosophers.arge.actor.annotations.Immutable;

/**
 * This division strategy changes divison number according to the input value
 * <b>DYNAMICALLY.</b> <br>
 * Uses deep neural network in order to do this.
 * 
 * @author osmanyasal
 *
 * @param <T>
 */
@Immutable
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AutoDivision<T> implements DivisionStrategy<T> {
	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	private Long queueLimit;

	@Override
	public boolean isConditionValid(Actor<T> actor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeSendingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeLoadingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		// TODO Auto-generated method stub

	}

}
