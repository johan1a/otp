package coordinate;

import com.google.gson.JsonObject;

public class WeightedCoordinate implements Comparable<WeightedCoordinate> {
	private Coordinate coordinate;
	private float weight;

	public WeightedCoordinate(Coordinate coordinate, float weight) {
		this.coordinate = coordinate;
		this.weight = weight;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public float getWeight() {
		return weight;
	}

	@Override
	public int compareTo(WeightedCoordinate other) {
		float otherWeight = other.getWeight();
		if (weight == otherWeight) {
			return 0;
		} else if (weight < otherWeight) {
			return -1;
		} else {
			return 1;
		}
	}

	public JsonObject getJsonObject() {
		return coordinate.getJsonObject();
	}
}
