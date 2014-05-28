package routeFinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RouteFinder {
	private static final String SERVER_URL = "http://localhost:8080/otp/routers/default";
	private static final String DEFAULT_OPTIONS = "&mode=TRANSIT,WALK&maxWalkDistance=750&arriveBy=false&showIntermediateStops=false&maxTransfers=0&clampInitialWait=0&numItineraries=1";
	private static final int GRID_CELL_COUNT = 10;
	private static final int GRID_RESOLUTION = 100; // meters per grid "cell"
	CloseableHttpClient httpclient;

	public TreeSet<WeightedCoordinate> getRankedCoordinates(Coordinate userPos,
			Coordinate friendPos) {
		httpclient = HttpClients.custom().build();
		// Coordinate userPos = new Coordinate(55.59435243706736,
		// 13.03253173828125);
		// Coordinate friendPos = new Coordinate(55.579120956833066,
		// 12.962493896484375);

		// Coordinate turningTorso = new Coordinate(55.613000, 12.976524);
		// Coordinate rosengard = new Coordinate(55.581963, 13.043815);

		// userPos = new Coordinate(55.600961, 12.985497);
		// friendPos = rosengard;

		// userPos = new Coordinate(55.568433, 12.967644);
		// friendPos = new Coordinate(55.604523, 13.017082);

		// userPos = turningTorso;
		ArrayList<Coordinate> mapGrid = createGrid(userPos, friendPos);

		HashMap<Coordinate, Float> userWeights = new HashMap<Coordinate, Float>();
		HashMap<Coordinate, Float> friendWeights = new HashMap<Coordinate, Float>();

		Route userRoute, friendRoute;
		System.out.println(mapGrid.size());

		float userDuration, friendDuration;
		TreeSet<WeightedCoordinate> weightedCoordinates = new TreeSet<WeightedCoordinate>();

		float bestWeight = Integer.MAX_VALUE, combinedWeight;
		Coordinate optimalCoordinate = null;
		for (Coordinate coordinate : mapGrid) {
			userRoute = searchForRoute(userPos, coordinate);
			friendRoute = searchForRoute(friendPos, coordinate);
			if (userRoute != null && friendRoute != null) {
				userDuration = userRoute.getDuration();
				friendDuration = friendRoute.getDuration();

				/*
				 * We don't want to multiply with zero (multiplied weight would
				 * also be zero)
				 */
				if (userDuration == 0) {
					userDuration = 1;
				}
				if (friendDuration == 0) {
					friendDuration = 1;
				}

				userWeights.put(coordinate, userDuration);
				friendWeights.put(coordinate, friendDuration);

				combinedWeight = calculateCombinedWeight(
						userWeights.get(coordinate),
						friendWeights.get(coordinate));

				weightedCoordinates.add(new WeightedCoordinate(coordinate,
						combinedWeight));

				System.out.println(userDuration + " " + friendDuration + " "
						+ coordinate + ", weight: " + combinedWeight);

				if (combinedWeight < bestWeight) {
					optimalCoordinate = coordinate;
					bestWeight = combinedWeight;
				}
			}
		}
		System.out.println("Optimal coordinate: " + optimalCoordinate
				+ ", weight " + bestWeight);

		System.out.println("From set: "
				+ weightedCoordinates.first().getCoordinate() + " "
				+ weightedCoordinates.first().getWeight());
		return weightedCoordinates;
	}

	private float calculateCombinedWeight(float w1, Float w2) {
		return Math.abs(1 - w1 / w2) * (w1 * w2) * (w1 * w2);
	}

	private ArrayList<Coordinate> createGrid(Coordinate origin,
			Coordinate destination) {

		Coordinate middle = getMiddle(origin, destination);

		double startLat = middle.getLat() - GRID_CELL_COUNT
				/ (GRID_RESOLUTION * 2.0);
		double startLon = middle.getLon() - GRID_CELL_COUNT
				/ (GRID_RESOLUTION * 2.0);

		ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

		Coordinate coordinate;
		for (int i = 0; i < GRID_CELL_COUNT; i++) {
			for (int j = 0; j < GRID_CELL_COUNT; j++) {

				coordinate = new Coordinate(startLat + i
						/ ((double) GRID_RESOLUTION), startLon + j
						/ ((double) GRID_RESOLUTION));
				coordinates.add(coordinate);
			}
		}
		return coordinates;
	}

	private Coordinate getMiddle(Coordinate origin, Coordinate destination) {
		double latDiff = origin.getLat() - destination.getLat();
		double lonDiff = origin.getLon() - destination.getLon();

		return new Coordinate(origin.getLat() + latDiff / 2, origin.getLon()
				+ lonDiff / 2);
	}

	private Route searchForRoute(Coordinate from, Coordinate to) {
		// System.out.println(HeatMapCreator.getRouteInfoQuery(from, to));
		HttpGet httpget = new HttpGet(RouteFinder.getRouteInfoQuery(from, to));
		Route route = null;
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);
			// System.out.println(response.getStatusLine());
			route = JSONHandler.parseShortestRoute(responseToString(response));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return route;
	}

	private static String responseToString(CloseableHttpResponse response) {
		HttpEntity responseEntity = response.getEntity();
		String result = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					responseEntity.getContent()));
			String line = "";
			do {

				result += line;
				line = reader.readLine();
			} while (line != null);

		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getRouteInfoQuery(Coordinate from, Coordinate to) {
		String dateString = getDateString();
		return SERVER_URL + "/plan/?fromPlace=" + from.getLat() + "%2C"
				+ from.getLon() + "&toPlace=" + to.getLat() + "%2C"
				+ to.getLon() + "&time=" + dateString + DEFAULT_OPTIONS;
	}

	private static String getDateString() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm MM-dd-yyyy");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime()).toString()
				.replaceAll(" ", "&date=");
	}

}
