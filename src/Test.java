import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//http://10.1.20.245:8000/test
//http://localhost:8000/test
public class Test {

	public static void main(String[] args) throws Exception {
		// HttpServer server = HttpServer.create(new InetSocketAddress(8000),
		// 0);
		// server.createContext("/test", new MyHandler());
		// server.setExecutor(null);
		// server.start();
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://www.openstreetmap.org/#map=11/55.5832/13.1383");

		HttpPost httpPost = new HttpPost(
				"http://www.openstreetmap.org/#map=11/55.5832/13.1383");

		System.out.println(httpPost.toString());
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			t.getRequestMethod();
			String response = "Like my bike";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

}