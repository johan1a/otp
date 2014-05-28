package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import pathFinding.PathFinder;
import util.JsonHandler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import coordinate.Coordinate;
import coordinate.WeightedCoordinate;

//http://localhost:9000/
public class Server {
	PathFinder routeFinder;

	public Server() {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(10000), 0);
			routeFinder = new PathFinder();
			server.createContext("/", new MyHandler());
			server.setExecutor(null);
			server.start();
			System.out.println("Server online!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) {
			URI uri = t.getRequestURI();

			List<Coordinate> coordinates = parseCoordinates(uri.getPath());
			String response;
			if (coordinates.size() == 2) {

				System.out.println("Searching for a meeting point...");
				long time = System.currentTimeMillis();
				TreeSet<WeightedCoordinate> weightedCoordinates = routeFinder
						.getCafesInMiddle(coordinates.get(0),
								coordinates.get(1));
				long executionTime = (System.currentTimeMillis() - time) / 1000;
				System.out.println("Completed in " + executionTime
						+ " seconds.");

				response = JsonHandler.createJsonArray(weightedCoordinates)
						.toString();

				System.out.println("Response: " + response);

			} else {
				response = "Error: Invalid URL";
			}

			Headers h = t.getResponseHeaders();
			h.add("Content-Type", "application/json; charset=utf-8");
			h.add("Access-Control-Allow-Origin", "*");
			h.add("Access-Control-Allow-Headers",
					"Origin, X-Requested-With, Content-Type, Accept");
			h.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

			OutputStream os = null;
			try {
				t.sendResponseHeaders(200, response.getBytes("utf-8").length);
				os = t.getResponseBody();
				os.write(response.getBytes("utf-8"));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
			System.err.println("Wrong URL format!");
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		Server server = new Server();
	}
}