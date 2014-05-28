package routeFinding;

public class Coordinate {
	private double lat, lon;

	public Coordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@Override
	public String toString() {
		return lat + "," + lon;
	}
}
