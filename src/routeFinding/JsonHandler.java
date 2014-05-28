package routeFinding;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonHandler {

	public static List<Coordinate> getCafeCoords() {
		String jsonString = "";
		try {
			jsonString = readFile("cafe-data.gradle", Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		JsonElement jelement = new JsonParser().parse(jsonString);
		JsonObject jobject = jelement.getAsJsonObject();
		jobject = jobject.getAsJsonObject("osm");

		JsonArray Coordinates;

		Coordinates = jobject.getAsJsonArray("node");
		JsonObject Coordinate;
		double lat, lon;

		ArrayList<Coordinate> CoordinateCoords = new ArrayList<Coordinate>();

		String name;
		for (int i = 0; i < Coordinates.size(); i++) {
			Coordinate = Coordinates.get(i).getAsJsonObject();
			lat = Coordinate.get("-lat").getAsDouble();
			lon = Coordinate.get("-lon").getAsDouble();

			name = getName(Coordinate);
			CoordinateCoords.add(new Coordinate(lat, lon, name));
		}
		return CoordinateCoords;
	}

	private static String getName(JsonObject Coordinate) {
		JsonArray properties = Coordinate.getAsJsonArray("tag");

		JsonObject propertyObject;
		for (int i = 0; i < properties.size(); i++) {
			propertyObject = properties.get(i).getAsJsonObject();
			if (propertyObject.get("-k").getAsString().equals("name")) {
				return propertyObject.get("-v").getAsString();
			}
		}
		return "";
	}

	/* Returns the shortest of the possible routes specified in the jsonString. */
	public static Route parseShortestRoute(String jsonString) {
		JsonArray routes = parsePossibleRoutes(jsonString);
		Route shortestRoute;
		if (routes == null) {
			shortestRoute = null;
		} else {
			shortestRoute = getShortestRoute(routes);
		}
		return shortestRoute;
	}

	public static JsonObject createJsonObject(
			TreeSet<WeightedCoordinate> weightedCoordinates) {
		JsonObject innerObject, jsonObject = new JsonObject();

		int i = 0;
		for (WeightedCoordinate coordinate : weightedCoordinates) {
			innerObject = coordinate.getInnerObject();
			jsonObject.add(Integer.toString(i++), innerObject);
		}
		return jsonObject;
	}

	/* Get in the car, there is no time to explain! */
	private static JsonArray parsePossibleRoutes(String jsonString) {
		JsonElement jelement = new JsonParser().parse(jsonString);
		JsonObject jobject = jelement.getAsJsonObject();
		jobject = jobject.getAsJsonObject("plan");

		JsonArray itineraries;
		try {
			itineraries = jobject.getAsJsonArray("itineraries");
		} catch (java.lang.NullPointerException e) {
			itineraries = null;
		}

		return itineraries;
	}

	public static Route getShortestRoute(JsonArray itineraries) {
		float min = Integer.MAX_VALUE, duration;
		JsonObject best = null;
		JsonObject itinerary;
		for (int i = 0; i < itineraries.size(); i++) {
			itinerary = itineraries.get(0).getAsJsonObject();
			duration = itinerary.get("duration").getAsInt();
			if (duration < min) {
				best = itinerary;
				min = duration;
			}
		}
		return new Route(best, min / 3600f);
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
