import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HeatMapCreator {
	private static final String SERVER_URL = "http://localhost:8080/otp/routers/default";
	private static final String DEFAULT_OPTIONS = "&mode=TRANSIT,WALK&maxWalkDistance=750&arriveBy=false&showIntermediateStops=false&maxTransfers=0";
	private static final int GRID_CELL_COUNT = 10;
	private static final int GRID_RESOLUTION = 100; // meters per grid "cell"
	CloseableHttpClient httpclient;

	public static void main(String[] args) {
		HeatMapCreator rs = new HeatMapCreator();
		rs.getOptimalCoordinate();
	}

	public void getOptimalCoordinate() {
		httpclient = HttpClients.custom().build();
		Coordinate userPos = new Coordinate(55.59435243706736,
				13.03253173828125);
		Coordinate friendPos = new Coordinate(55.579120956833066,
				12.962493896484375);

		ArrayList<Coordinate> mapGrid = createGrid(userPos, friendPos);

		HashMap<Coordinate, Float> userWeights = new HashMap<Coordinate, Float>();
		HashMap<Coordinate, Float> friendWeights = new HashMap<Coordinate, Float>();

		Route userRoute, friendRoute;
		System.out.println(mapGrid.size());

		float userDuration, friendDuration;

		HashMap<Coordinate, Float> multipliedWeights = new HashMap<Coordinate, Float>();
		float minDuration = Integer.MAX_VALUE, multipliedDuration;
		Coordinate optimalCoordinate = null;
		Route bestRoute = null;
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

				multipliedDuration = userWeights.get(coordinate)
						* friendWeights.get(coordinate);

				multipliedWeights.put(coordinate, multipliedDuration);

				System.out.println(userDuration + " " + friendDuration + " "
						+ coordinate + ", weight: " + multipliedDuration);

				if (multipliedDuration < minDuration) {
					optimalCoordinate = coordinate;
					minDuration = multipliedDuration;
				}
			}
		}
		System.out.println("Optimal coordinate: " + optimalCoordinate
				+ ", duration " + minDuration);
	}

	private ArrayList<Coordinate> createGrid(Coordinate origin,
			Coordinate destination) {
		double startLat = origin.getLat() - GRID_CELL_COUNT
				/ (GRID_RESOLUTION * 2.0);
		double startLon = origin.getLon() - GRID_CELL_COUNT
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

	private Route searchForRoute(Coordinate from, Coordinate to) {
		// System.out.println(HeatMapCreator.getRouteInfoQuery(from, to));
		HttpGet httpget = new HttpGet(
				HeatMapCreator.getRouteInfoQuery(from, to));
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

	public void makeMapGrid(Coordinate center) {

	}

}
