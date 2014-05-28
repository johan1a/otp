package routeFinding;

import com.google.gson.JsonObject;

public class Coordinate {
	private double lat, lon;
	private String name = "name"; // hax yeah

	public Coordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public Coordinate(double lat, double lon, String name) {
		this.lat = lat;
		this.lon = lon;
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return lat + "," + lon;
	}

	public String getName() {
		return name;
	}

	public JsonObject getInnerObject() {
		JsonObject innerObject = new JsonObject();
		innerObject.addProperty("coordinate", toString());
		return innerObject;
	}
}
