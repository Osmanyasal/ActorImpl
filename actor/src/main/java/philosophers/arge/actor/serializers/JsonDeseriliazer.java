package philosophers.arge.actor.serializers;

public interface JsonDeseriliazer<T> {
	T fromJson(String json);
}
