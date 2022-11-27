package ca.utoronto.utm.mcs;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Please write your tests in this class. 
 */



public class AppTest {

    public HttpResponse<String> sendPutReq(HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .headers("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> sendGetReq(HttpClient client, String uriEndPoint) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .method("GET", noBody())
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> sendPostReq(HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .headers("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    // for all except for get and put and post
    public HttpResponse<String> sendHTTPReq(String method, HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .method(method, HttpRequest.BodyPublishers.ofString(body)).version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void getNearbyDriverPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"2\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/1", "{\n" +
                " 'longitude': 1.0,\n" +
                " 'latitude': 1.0,\n" +
                " 'street': 'Street 1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/2", "{\n" +
                " 'longitude': 2.0,\n" +
                " 'latitude': 2.0,\n" +
                " 'street': 'Street 2'\n" +
                "}");
        HttpResponse response = sendGetReq(client, "http://localhost:8000/location/nearbyDriver/1?radius=2");
        String receivedResponse = response.body().toString();
                System.out.println(receivedResponse);
        String correctResponse = "{\"data\":{\"2\":{\"street\":\"Street 2\",\"latitude\":2,\"longitude\":2}},\"status\":\"OK\"}";

        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void getNearbyDriverFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"3\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"4\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/3", "{\n" +
                " 'longitude': 3.0,\n" +
                " 'latitude': 3.0,\n" +
                " 'street': 'Street 1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/4", "{\n" +
                " 'longitude': 4.0,\n" +
                " 'latitude': 4.0,\n" +
                " 'street': 'Street 3'\n" +
                "}");
        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/nearbyDriver/11123123123123212?radius=1");
        String receivedResponse = response.body().toString();
//        System.out.println(receivedResponse);
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }

    @Test
    public void getNavigationPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"5\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"6\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"7\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"8\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/5", "{\n" +
                " 'longitude': 5.0,\n" +
                " 'latitude': 5.0,\n" +
                " 'street': 'Street 5'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/6", "{\n" +
                " 'longitude': 6.0,\n" +
                " 'latitude': 6.0,\n" +
                " 'street': 'Street 6'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/7", "{\n" +
                " 'longitude': 7.0,\n" +
                " 'latitude': 7.0,\n" +
                " 'street': 'Street 7'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/8", "{\n" +
                " 'longitude': 8.0,\n" +
                " 'latitude': 8.0,\n" +
                " 'street': 'Street 8'\n" +
                "}");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 5\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 6\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 7\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 8\", \"hasTraffic\" : false }");

        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 5\", \"roadName2\": \"Street 6\", \"hasTraffic\" : true, \"time\": 3 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 6\", \"roadName2\": \"Street 7\", \"hasTraffic\" : true, \"time\": 4 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 7\", \"roadName2\": \"Street 8\", \"hasTraffic\" : true, \"time\": 7 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 5\", \"roadName2\": \"Street 8\", \"hasTraffic\" : true, \"time\": 50 }");

        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/navigation/8?passengerUid=5");
        String receivedResponse = response.body().toString();
        String correctResponse = "{\"data\":{\"route\":[{\"street\":\"Street 5\",\"is_traffic\":true,\"time\":0},{\"street\":\"Street 6\",\"is_traffic\":true,\"time\":3},{\"street\":\"Street 7\",\"is_traffic\":true,\"time\":4},{\"street\":\"Street 8\",\"is_traffic\":true,\"time\":7}],\"total_time\":\"14\"},\"status\":\"OK\"}";
        System.out.println(receivedResponse);
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void getNavigationFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/navigation/nav1123123?passengerUid=nav4");
        String receivedResponse = response.body().toString();
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }
}
