package routeFinding;

import java.util.Comparator;

public class WeightedCoordinateComparator implements
		Comparator<WeightedCoordinate> {
	public int compare(WeightedCoordinate w1, WeightedCoordinate w2) {
		return (int) Math.round((w1.getWeight() - w2.getWeight()));
	}
}