package par.core.actor.divisionstrategies;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;
import par.core.actor.annotations.NotImplemented;
import par.core.actor.base.ActorMessage;
import par.core.actor.base.node.Actor;

/**
 * This division strategy changes divison number according to the input value
 * <b>DYNAMICALLY.</b> <br>
 * Uses deep neural network in order to do this.
 * 
 * @author osmanyasal
 *
 * @param <T>
 */

//TODO:implement autodivision
@NotImplemented
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
		return false;
	}

	@Override
	public void executeSendingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		return;
	}

	@Override
	public void executeLoadingStrategy(Actor<T> actor, List<ActorMessage<T>> message) {
		return;
	}

}
