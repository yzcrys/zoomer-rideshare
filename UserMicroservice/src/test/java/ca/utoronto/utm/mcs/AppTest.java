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
                .timeout(Duration.ofSeconds(20))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> sendGetReq(HttpClient client, String uriEndPoint) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .method("GET", noBody())
                .timeout(Duration.ofSeconds(20))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> sendPostReq(HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .headers("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(20))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    // for all except for get and put and post
    public HttpResponse<String> sendHTTPReq(String method, HttpClient client, String uriEndPoint, String body) throws URISyntaxException, IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(new URI(uriEndPoint))
                .method(method, HttpRequest.BodyPublishers.ofString(body)).version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(20))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void userRegisterPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = sendHTTPReq("POST", client, "http://localhost:8004/user/register", "{\n" +
                "\t\"name\": \"Test 123\", \n" +
                "\t\"email\": \"123@mail.com\", \n" +
                "\t\"password\" : \"test\"\n" +
                "}");

        String receivedResponse = response.body().toString();
        String correctResponse = "{\"status\":\"OK\"}";
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void userRegisterFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            sendHTTPReq("POST", client, "http://localhost:8004/user/register", "{\n" +
                    "\t\"name\": \"Test 321\", \n" +
                    "\t\"email\": \"321@mail.com\", \n" +
                    "\t\"password\" : \"test\"\n" +
                    "}");
            HttpResponse response = sendHTTPReq("POST", client, "http://localhost:8004/user/register", "{\n" +
                    "\t\"nameTest 321\", \n" +
                    "\t\"email\": \"321@mail.com\", \n" +
                    "\t\"password\" : \"test\"\n" +
                    "}");

            String receivedResponse = response.body().toString();
            String correctResponse = "{\"status\":\"BAD REQUEST\"}";
            assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 400);
        } catch (Exception e) {
        }
    }

    @Test
    public void userLoginPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendHTTPReq("POST", client, "http://localhost:8004/user/register", "{\n" +
                "\t\"name\": \"Test 123123\", \n" +
                "\t\"email\": \"123123@mail.com\", \n" +
                "\t\"password\" : \"test\"\n" +
                "}");
        HttpResponse response = sendHTTPReq("POST", client, "http://localhost:8004/user/login", "{\n" +
                "\t\"email\": \"123123@mail.com\", \n" +
                "\t\"password\" : \"test\"\n" +
                "}");

        String receivedResponse = response.body().toString();
        String correctResponse = "{\"status\":\"OK\"}";
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void userLoginFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = sendHTTPReq("POST", client, "http://localhost:8004/user/login", "{\n" +
                "\t\"email\": \"321@dfaasdf.co*&^*8ism\", \n" +
                "\t\"password\" : \"tesa&ASD(AShdkajsdnlasdy^A*S^sdt\"\n" +
                "}");

        String receivedResponse = response.body().toString();
        String correctResponse = "{\"status\":\"NOT FOUND\"}";
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 404);
    }
}
