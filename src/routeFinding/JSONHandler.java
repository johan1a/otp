package routeFinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONHandler {

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
}
