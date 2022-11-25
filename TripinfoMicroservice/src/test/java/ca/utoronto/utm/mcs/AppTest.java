package ca.utoronto.utm.mcs;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {
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

    @Test
    public void tripRequestPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"req1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"req2\", \"is_driver\" : true }");
        sendPostReq( client, "http://localhost:8002/trip/request", "{ \"uid\": \"req1\", \"radius\": 2 }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/req1", "{\n" +
                " 'longitude': 50.1,\n" +
                " 'latitude': 50.1,\n" +
                " 'street': 'Street req1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/req2", "{\n" +
                " 'longitude': 51.2,\n" +
                " 'latitude': 51.2,\n" +
                " 'street': 'Street req2'\n" +
                "}");

        HttpResponse response = sendGetReq(client, "http://localhost:8000/location/nearbyDriver/req1?radius=5");
        String receivedResponse = response.body().toString();
//        System.out.println(receivedResponse + ", " + response.statusCode());
        String correctResponse = "{\"data\":{\"req2\":{\"street\":\"Street req2\",\"latitude\":51.2,\"longitude\":51.2}},\"status\":\"OK\"}";
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void tripRequestFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"reqf1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"reqf2\", \"is_driver\" : true }");
        sendPostReq( client, "http://localhost:8002/trip/request", "{ \"uid\": \"reqf1\", \"radius\": 2 }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/reqf1", "{\n" +
                " 'longitude': 1.1,\n" +
                " 'latitude': 1.1,\n" +
                " 'street': 'Street req1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/reqf2", "{\n" +
                " 'longitude': 2.2,\n" +
                " 'latitude': 2.2,\n" +
                " 'street': 'Street req2'\n" +
                "}");

        HttpResponse response = sendGetReq(client, "http://localhost:8000/location/nearbyDriver/re12q1?radius=1");
        String receivedResponse = response.body().toString();
//        System.out.println(receivedResponse + ", " + response.statusCode());
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }
}
