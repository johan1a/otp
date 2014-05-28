package util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;

import pathFinding.Route;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coordinate.CafeCoordinate;
import coordinate.WeightedCoordinate;

public class JsonHandler {
	private ArrayList<CafeCoordinate> cafeCoords;

	public JsonHandler() {
		String jsonString = "";
		try {
			jsonString = readFile("cafe-data.gradle", Charset.forName("utf-8"));
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

		cafeCoords = new ArrayList<CafeCoordinate>();

		String name;
		for (int i = 0; i < Coordinates.size(); i++) {
			Coordinate = Coordinates.get(i).getAsJsonObject();
			lat = Coordinate.get("-lat").getAsDouble();
			lon = Coordinate.get("-lon").getAsDouble();

			name = getName(Coordinate);
			cafeCoords.add(new CafeCoordinate(lat, lon, name));
		}
	}

	public ArrayList<CafeCoordinate> getAllCafeCoords() {
		return cafeCoords;
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
	public static Route parseRoute(String jsonString) {
		JsonArray routes = parsePossibleRoutes(jsonString);
		Route route;
		if (routes == null) {
			route = null;
		} else {
			JsonObject routeString = routes.get(0).getAsJsonObject();
			float duration = routeString.get("duration").getAsInt();
			route = new Route(routeString, duration / 3600f);
		}
		return route;
	}

	public static JsonArray createJsonArray(
			TreeSet<WeightedCoordinate> weightedCoordinates) {
		JsonObject innerObject;
		JsonArray array = new JsonArray();

		for (WeightedCoordinate coordinate : weightedCoordinates) {
			innerObject = coordinate.getJsonObject();
			array.add(innerObject);
		}
		return array;
	}

	/* Returns all the itineraries from the OTP Json data. */
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

	private static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
