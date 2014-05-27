package routeFinding;
import java.util.Comparator;

public class WeightedCoordinate {
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

	class FairComparator implements Comparator<WeightedCoordinate> {

		@Override
		public int compare(WeightedCoordinate o1, WeightedCoordinate o2) {
			return Math.round(Math.abs(o1.getWeight() - 1)
					- Math.abs(o2.getWeight() - 1));
		}
	}

}
