package par.core.actor.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import par.core.actor.annotations.Immutable;

@Immutable
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class Topic {

	@Setter(AccessLevel.PRIVATE)
	private String name;
}
