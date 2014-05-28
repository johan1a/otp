package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.client.methods.HttpPost;

import routeFinding.Coordinate;
import routeFinding.RouteFinder;
import routeFinding.WeightedCoordinate;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//http://localhost:9000/
public class Server {
	RouteFinder routeFinder;

	public Server() {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(9000), 0);
			routeFinder = new RouteFinder();
			server.createContext("/", new MyHandler());
			server.setExecutor(null);
			server.start();

			HttpPost httpPost = new HttpPost(
					"http://www.openstreetmap.org/#map=11/55.5832/13.1383");
			System.out.println(httpPost.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
	}

	class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			URI uri = t.getRequestURI();

			List<Coordinate> coordinates = parseCoordinates(uri.getPath());

			TreeSet<WeightedCoordinate> weightedCoordinates = routeFinder
					.getRankedCoordinates(coordinates.get(0),
							coordinates.get(1));

			JsonObject jsonObject = new JsonObject();
			JsonObject innerObject;
			int i = 0;
			for (WeightedCoordinate coordinate : weightedCoordinates) {
				innerObject = new JsonObject();
				innerObject.addProperty("coordinate", coordinate
						.getCoordinate().toString());

				jsonObject.add(Integer.toString(i++), innerObject);
			}

			String response = jsonObject.toString();

			Headers h = t.getResponseHeaders();
			h.add("Content-Type", "application/json; charset=UTF-8");
			h.add("Access-Control-Allow-Origin", "*");
			h.add("Access-Control-Allow-Headers",
					"Origin, X-Requested-With, Content-Type, Accept");
			h.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();

			os.write(response.getBytes("UTF-8"));
			os.close();
		}
	}

	public static List<Coordinate> parseCoordinates(String path) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		try {
			String temp = path.replace("/", "");
			String[] coordStrings = temp.split(":");

			for (int i = 0; i < 2; i++) {
				String[] latLon = coordStrings[i].split(",");
				double lat = Double.parseDouble(latLon[0]);
				double lon = Double.parseDouble(latLon[1]);
				result.add(new Coordinate(lat, lon));
			}
		} catch (NumberFormatException e) {

		}
		return result;
	}
}