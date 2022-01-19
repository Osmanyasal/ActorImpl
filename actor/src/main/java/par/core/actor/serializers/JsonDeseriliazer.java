package par.core.actor.serializers;

public interface JsonDeseriliazer<T> {
	T fromJson(String json);
}
