package pathFinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import util.JsonHandler;

import coordinate.Coordinate;
import coordinate.WeightedCoordinate;

public class PathFinder {
	private static final String SERVER_URL = "http://localhost:8080/otp/routers/default";
	private static final String DEFAULT_OPTIONS = "&mode=TRANSIT,WALK&maxWalkDistance=750&arriveBy=false&showIntermediateStops=false&maxTransfers=0&clampInitialWait=0&numItineraries=1";
	private static final int GRID_CELL_COUNT = 10;
	private static final int GRID_RESOLUTION = 100; // meters per grid "cell"
	private CloseableHttpClient httpclient;
	private String dateString;
	private JsonHandler jsonHandler;

	public PathFinder() {
		httpclient = HttpClients.custom().build();
		jsonHandler = new JsonHandler();
	}

	/*
	 * Returns a list of cafes, sorted by the combined travel duration.
	 */
	public TreeSet<WeightedCoordinate> getCafesInMiddle(Coordinate userPos,
			Coordinate friendPos) {
		return getBestMiddlePoints(userPos, friendPos,
				jsonHandler.getAllCafeCoords());
	}

	/*
	 * Returns a list of coordinates, sorted by the combined travel duration.
	 * Uses a 'heatmap'.
	 */
	public TreeSet<WeightedCoordinate> getMeetingPointByGrid(
			Coordinate userPos, Coordinate friendPos) {
		return getBestMiddlePoints(userPos, friendPos,
				createGrid(userPos, friendPos));
	}

	/* Returns a set of the best middle points. */
	public TreeSet<WeightedCoordinate> getBestMiddlePoints(Coordinate userPos,
			Coordinate friendPos, List<? extends Coordinate> pointsToCheck) {
		Route userRoute, friendRoute;

		TreeSet<WeightedCoordinate> weightedCoordinates = new TreeSet<WeightedCoordinate>();
		float userDuration, friendDuration;
		float bestWeight = Integer.MAX_VALUE, combinedWeight;
		Coordinate optimalCoordinate = null;
		initDateString();
		for (Coordinate coordinate : pointsToCheck) {
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

				combinedWeight = calculateCombinedWeight(userDuration,
						friendDuration);

				weightedCoordinates.add(new WeightedCoordinate(coordinate,
						combinedWeight));

				// System.out.println(userDuration + " " + friendDuration + " "
				// + coordinate + ", weight: " + combinedWeight);

				if (combinedWeight < bestWeight) {
					optimalCoordinate = coordinate;
					bestWeight = combinedWeight;
				}
			}
		}
		System.out.println("Optimal coordinate: " + optimalCoordinate
				+ ", weight " + bestWeight);

		return weightedCoordinates;
	}

	private void initDateString() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm MM-dd-yyyy");
		Calendar cal = Calendar.getInstance();
		dateString = dateFormat.format(cal.getTime()).toString()
				.replaceAll(" ", "&date=");
	}

	private float calculateCombinedWeight(float w1, Float w2) {
		return Math.abs(1 - w1 / w2) * (w1 * w2);
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
		HttpGet httpget = new HttpGet(getRouteInfoQuery(from, to));
		Route route = null;
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);
			route = JsonHandler.parseRoute(responseToString(response));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return route;
	}

	public String getRouteInfoQuery(Coordinate from, Coordinate to) {
		return SERVER_URL + "/plan/?fromPlace=" + from.getLat() + "%2C"
				+ from.getLon() + "&toPlace=" + to.getLat() + "%2C"
				+ to.getLon() + "&time=" + dateString + DEFAULT_OPTIONS;
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

}
