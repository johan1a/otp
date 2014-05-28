package coordinate;

import com.google.gson.JsonObject;

public class CafeCoordinate extends Coordinate {
	private String name;

	public CafeCoordinate(double lat, double lon, String name) {
		super(lat, lon);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public JsonObject getJsonObject() {
		JsonObject innerObject = new JsonObject();
		innerObject.addProperty("coordinate", toString());
		innerObject.addProperty("name", name);
		return innerObject;
	}
}
