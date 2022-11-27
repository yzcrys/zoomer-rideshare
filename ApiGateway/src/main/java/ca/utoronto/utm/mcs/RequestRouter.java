package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response
import java.util.HashMap;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */

	public HashMap<Integer, String> errorMap;

	public RequestRouter() {
		errorMap = new HashMap<>();
		errorMap.put(200, "OK");
		errorMap.put(400, "BAD REQUEST");
		errorMap.put(401, "UNAUTHORIZED");
		errorMap.put(404, "NOT FOUND");
		errorMap.put(405, "METHOD NOT ALLOWED");
		errorMap.put(409, "CONFLICT");
		errorMap.put(500, "INTERNAL SERVER ERROR");
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
        String method = r.getRequestMethod();
		String endPoint = r.getRequestURI().toString();
		String uri = "";
		if (endPoint.contains("location"))
			uri = "http://locationmicroservice:8000" + endPoint;
		else if (endPoint.contains("trip"))
			uri = "http://tripinfomicroservice:8002" + endPoint;
		else if (endPoint.contains("user"))
			uri = "http://usermicroservice:8001" + endPoint;

		HttpClient client = HttpClient.newHttpClient();

		System.out.println("IN API GATEWAY" + uri + ", " + method + "\n");

		switch (method) {
			case "GET" -> {
				HttpResponse response = null;
				try {
					response = sendGetReq(client, uri);
					this.sendResponse(r, new JSONObject(response.body().toString()), response.statusCode());

				} catch (URISyntaxException | InterruptedException | JSONException e) {
					try {
						this.sendStatus(r, response.statusCode());
					} catch (JSONException ex) {
						throw new RuntimeException(ex);
					}
					throw new RuntimeException(e);
				}
			}
			case "PUT" -> {
				HttpResponse response = null;
				try {
					String reqBody = Utils.convert(r.getRequestBody());
					response = sendPutReq(client, uri, reqBody);
					this.sendResponse(r, new JSONObject(response.body().toString()), response.statusCode());

				} catch (InterruptedException | URISyntaxException | JSONException e) {
					try {
						this.sendStatus(r, response.statusCode());
					} catch (JSONException ex) {
						throw new RuntimeException(ex);
					}
					throw new RuntimeException(e);
				}
			}
			case "POST" -> {
				HttpResponse response = null;
				try {
					String reqBody = Utils.convert(r.getRequestBody());
					response = sendPostReq(client, uri, reqBody);

					if (response.body() == null || response.body().toString().isEmpty())
						this.sendStatus(r, response.statusCode());
					else
						this.sendResponse(r, new JSONObject(response.body().toString()), response.statusCode());

				} catch (InterruptedException | URISyntaxException | JSONException e) {
					try {
						this.sendStatus(r, response.statusCode());
					} catch (JSONException ex) {
						throw new RuntimeException(ex);
					}
					throw new RuntimeException(e);
				}
			}
			case "DELETE", "PATCH" -> {
				HttpResponse response = null;
				try {
					String reqBody = Utils.convert(r.getRequestBody());
					System.out.println("before sending request");
					response = sendHTTPReq(method, client, uri, reqBody);
					;
					System.out.println("apigateway response: " + response.body().toString());
					System.out.println("\napigateway status code: " + response.statusCode());
					if (response.body() == null || response.body().toString().isEmpty())
						this.sendStatus(r, response.statusCode());
					else
						this.sendResponse(r, new JSONObject(response.body().toString()), response.statusCode());

				} catch (InterruptedException | URISyntaxException | JSONException e) {
					try {
						this.sendStatus(r, response.statusCode());
					} catch (JSONException ex) {
						throw new RuntimeException(ex);
					}
					throw new RuntimeException(e);
				}
			}
			default -> {
//                System.out.println("ReqHandler: handleGet() Error");
				try {
					this.sendStatus(r, 404);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void writeOutputStream(HttpExchange r, String response) throws IOException {
		OutputStream os = r.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
		obj.put("status", errorMap.get(statusCode));
		String response = obj.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

	public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
		JSONObject res = new JSONObject();
		res.put("status", errorMap.get(statusCode));
		String response = res.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

	public HttpResponse<String> sendPutReq(HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder()
				.uri(new URI(uriEndPoint))
				.headers("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(body))
				.build(), HttpResponse.BodyHandlers.ofString());
	}

	public HttpResponse<String> sendGetReq(HttpClient client, String uriEndPoint) throws URISyntaxException, IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder()
				.uri(new URI(uriEndPoint))
				.method("GET", noBody())
				.build(), HttpResponse.BodyHandlers.ofString());
	}

	public HttpResponse<String> sendPostReq(HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder()
				.uri(new URI(uriEndPoint))
				.headers("Content-Type", "application/json")
				.method("POST", HttpRequest.BodyPublishers.ofString(body))
				.build(), HttpResponse.BodyHandlers.ofString());
	}

	// for all except for get and put and post
	public HttpResponse<String> sendHTTPReq(String method, HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder()
				.uri(new URI(uriEndPoint))
				.method(method, HttpRequest.BodyPublishers.ofString(body))
				.build(), HttpResponse.BodyHandlers.ofString());
	}
}
