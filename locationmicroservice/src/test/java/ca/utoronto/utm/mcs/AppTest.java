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
    public void getNearbyDriverPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"2\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/1", "{\n" +
                " 'longitude': 1.1,\n" +
                " 'latitude': 1.1,\n" +
                " 'street': 'Street 1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/2", "{\n" +
                " 'longitude': 3.1,\n" +
                " 'latitude': 3.1,\n" +
                " 'street': 'Street 3'\n" +
                "}");
        HttpResponse response = sendGetReq(client, "http://localhost:8000/location/nearbyDriver/1?radius=1");
        String receivedResponse = response.body().toString();
        String correctResponse = "{\"data\":{\"0\":{\"street\":\"Street 1\",\"latitude\":1.1,\"longitude\":1.1}},\"status\":\"OK\"}";

        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void getNearbyDriverFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"2\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/1", "{\n" +
                " 'longitude': 1.1,\n" +
                " 'latitude': 1.1,\n" +
                " 'street': 'Street 1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/2", "{\n" +
                " 'longitude': 3.1,\n" +
                " 'latitude': 3.1,\n" +
                " 'street': 'Street 3'\n" +
                "}");
        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/nearbyDriver/112?radius=1");
        String receivedResponse = response.body().toString();
//        System.out.println(receivedResponse);
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }

    @Test
    public void getNavigationPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"nav1\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"nav2\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"nav3\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"nav4\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/nav1", "{\n" +
                " 'longitude': 1.1,\n" +
                " 'latitude': 1.1,\n" +
                " 'street': 'Street nav1'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/nav2", "{\n" +
                " 'longitude': 2.1,\n" +
                " 'latitude': 2.1,\n" +
                " 'street': 'Street nav2'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/nav3", "{\n" +
                " 'longitude': 3.1,\n" +
                " 'latitude': 3.1,\n" +
                " 'street': 'Street nav3'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/nav4", "{\n" +
                " 'longitude': 4.1,\n" +
                " 'latitude': 4.1,\n" +
                " 'street': 'Street nav4'\n" +
                "}");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street nav1\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street nav2\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street nav3\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street nav4\", \"hasTraffic\" : false }");

        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street nav1\", \"roadName2\": \"Street nav4\", \"hasTraffic\" : true, \"time\": 50 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street nav1\", \"roadName2\": \"Street nav2\", \"hasTraffic\" : true, \"time\": 4 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street nav2\", \"roadName2\": \"Street nav3\", \"hasTraffic\" : true, \"time\": 7 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street nav3\", \"roadName2\": \"Street nav4\", \"hasTraffic\" : true, \"time\": 3 }");

        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/navigation/nav1?passengerUid=nav4");
        String receivedResponse = response.body().toString();
        String correctResponse = "{\"data\":{\"route\":[{\"street\":\"Street nav1\",\"is_traffic\":true,\"time\":0},{\"street\":\"Street nav4\",\"is_traffic\":true,\"time\":50}],\"total_time\":\"50\"},\"status\":\"OK\"}";
//        System.out.println(receivedResponse);
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
