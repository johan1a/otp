package routeFinding;

import java.util.TreeSet;

public class Test {
	public static void main(String[] args) {
		WeightedCoordinate w;
		TreeSet<WeightedCoordinate> set = new TreeSet<WeightedCoordinate>();
		for (int i = 0; i < 50; i++) {
			float weight = (float) Math.random();
			w = new WeightedCoordinate(null, weight);
			set.add(w);

			System.out.println(weight + " " + set.first().getWeight());
		}
	}
}
