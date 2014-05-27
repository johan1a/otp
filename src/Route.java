import com.google.gson.JsonObject;

public class Route {
	private JsonObject data; // ifall att.... trololol
	private float duration; // in hours

	public Route(JsonObject best, float duration) {
		this.data = best;
		this.duration = duration;
	}

	public String toString() {
		return Float.toString(duration);
	}

	public float getDuration() {
		return duration;
	}

}
