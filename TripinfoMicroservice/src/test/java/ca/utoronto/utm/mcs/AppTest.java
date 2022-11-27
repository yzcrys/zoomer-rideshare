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
    public void tripRequestPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"10\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"11\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"12\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"13\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/10", "{\n" +
                " 'longitude': 10.0,\n" +
                " 'latitude': 10.0,\n" +
                " 'street': 'Street 10'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/11", "{\n" +
                " 'longitude': 11.0,\n" +
                " 'latitude': 11.0,\n" +
                " 'street': 'Street 11'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/12", "{\n" +
                " 'longitude': 12.0,\n" +
                " 'latitude': 12.0,\n" +
                " 'street': 'Street 12'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/13", "{\n" +
                " 'longitude': 13.0,\n" +
                " 'latitude': 13.0,\n" +
                " 'street': 'Street 13'\n" +
                "}");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 10\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 11\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 12\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 13\", \"hasTraffic\" : false }");

        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 10\", \"roadName2\": \"Street 11\", \"hasTraffic\" : true, \"time\": 3 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 11\", \"roadName2\": \"Street 12\", \"hasTraffic\" : true, \"time\": 4 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 12\", \"roadName2\": \"Street 13\", \"hasTraffic\" : true, \"time\": 7 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 10\", \"roadName2\": \"Street 13\", \"hasTraffic\" : true, \"time\": 50 }");

        HttpResponse response = sendGetReq(client, "http://localhost:8004/location/navigation/13?passengerUid=10");
        String receivedResponse = response.body().toString();
        String correctResponse = "{\"data\":{\"route\":[{\"street\":\"Street 10\",\"is_traffic\":true,\"time\":0},{\"street\":\"Street 11\",\"is_traffic\":true,\"time\":3},{\"street\":\"Street 12\",\"is_traffic\":true,\"time\":4},{\"street\":\"Street 13\",\"is_traffic\":true,\"time\":7}],\"total_time\":\"14\"},\"status\":\"OK\"}";
        System.out.println(receivedResponse);
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void tripRequestFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = sendPostReq(client, "http://localhost:8004/trip/request", "{\n" +
                "\t\"uid\": \"346678422\", \n" +
                "\t\"radius\": 2\n" +
                "}");

        String receivedResponse = response.body().toString();
//        System.out.println(receivedResponse + ", " + response.statusCode());
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }

    @Test
    public void tripConfirmPass() throws URISyntaxException, IOException, InterruptedException, JSONException {

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse response = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                "\t\"driver\": \"ab\", \n" +
                "\t\"passenger\": \"ba\",\n" +
                "\t\"startTime\": 1669342329\n" +
                "}");

        String receivedResponse = response.body().toString();
        System.out.println(receivedResponse + ", " + response.statusCode());
        assertTrue(!receivedResponse.isEmpty() && response.statusCode() == 200);
    }

    @Test
    public void tripConfirmFail() throws URISyntaxException, IOException, InterruptedException, JSONException {

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse response = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                "\t\"drivser\": \"ab\", \n" +
                "\t\"passenger\": \"ba\",\n" +
                "\t\"startTime\": 1669342329\n" +
                "}");

        String receivedResponse = response.body().toString();
        assertTrue(receivedResponse.equals("{\"status\":\"BAD REQUEST\"}") && response.statusCode() == 400);
    }

    @Test
    public void patchTripPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse response1 = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                    "\t\"driver\": \"202342234\", \n" +
                    "\t\"passenger\": \"212234234\",\n" +
                    "\t\"startTime\": 1669342329\n" +
                    "}");

            JSONObject obj = null;
            try {
                obj = new JSONObject(response1.body().toString());
            }
            catch (Exception e)
            {
                assertTrue(false);
            }
            String tripId = obj.getJSONObject("_id").getString("$oid");

            HttpResponse response = sendHTTPReq("PATCH", client, "http://localhost:8004/trip/" + tripId, "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            String receivedResponse = response.body().toString();
//            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(response.statusCode() == 200);
        }
        catch (Exception e) {
        }
    }

    @Test
    public void patchTripFail() throws URISyntaxException, IOException, InterruptedException, JSONException {

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse response = sendHTTPReq("PATCH", client, "http://localhost:8004/trip/8934kjsndjkndsgaj7", "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            String receivedResponse = response.body().toString();
            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(receivedResponse.equals("{\"status\":\"BAD REQUEST\"}") && response.statusCode() == 400);
        } catch (Exception e) {
        }
    }

    @Test
    public void tripsForPassengerPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse response1 = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                    "\t\"driver\": \"284921\", \n" +
                    "\t\"passenger\": \"849002\",\n" +
                    "\t\"startTime\": 1669342329\n" +
                    "}");

            JSONObject obj = null;
            try {
                obj = new JSONObject(response1.body().toString());
            }
            catch (Exception e)
            {
                assertTrue(false);
            }
            String tripId = obj.getJSONObject("_id").getString("$oid");

            sendHTTPReq("PATCH", client, "http://localhost:8004/trip/" + tripId, "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/passenger/849002");
            String receivedResponse = response.body().toString();
//            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(response.statusCode() == 200);
        }
        catch (Exception e) {
        }
    }

    @Test
    public void tripsForPassengerFail() throws URISyntaxException, IOException, InterruptedException, JSONException {

        try {
            HttpClient client = HttpClient.newHttpClient();

            sendHTTPReq("PATCH", client, "http://localhost:8004/trip/123124", "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/passenger/8654323458456");
            String receivedResponse = response.body().toString();
            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
        } catch (Exception e) {
        }
    }

    @Test
    public void tripsForDriverPass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse response1 = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                    "\t\"driver\": \"423672\", \n" +
                    "\t\"passenger\": \"634521\",\n" +
                    "\t\"startTime\": 1669342329\n" +
                    "}");

            JSONObject obj = null;
            try {
                obj = new JSONObject(response1.body().toString());
            }
            catch (Exception e)
            {
                assertTrue(false);
            }
            String tripId = obj.getJSONObject("_id").getString("$oid");

            sendHTTPReq("PATCH", client, "http://localhost:8004/trip/" + tripId, "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/driver/423672");
            String receivedResponse = response.body().toString();
//            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(response.statusCode() == 200);
        }
        catch (Exception e) {
        }
    }

    @Test
    public void tripsForDriverFail() throws URISyntaxException, IOException, InterruptedException, JSONException {

        try {
            HttpClient client = HttpClient.newHttpClient();

            sendHTTPReq("PATCH", client, "http://localhost:8004/trip/123124", "{\n" +
                    "\t\"distance\": 12, \n" +
                    "\t\"endTime\": 14,\n" +
                    "\t\"timeElapsed\": 40,\n" +
                    "\t\"discount\": 12.0,\n" +
                    "\t\"totalCost\": 30.0,\n" +
                    "\t\"driverPayout\": 12.0\n" +
                    "}");

            HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/driver/8654323458456");
            String receivedResponse = response.body().toString();
            System.out.println(receivedResponse + ", " + response.statusCode());
            assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
        } catch (Exception e) {
        }
    }

    @Test
    public void driverTimePass() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"30\", \"is_driver\" : false }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"31\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"32\", \"is_driver\" : true }");
        sendPutReq(client, "http://localhost:8004/location/user", "{ \"uid\": \"33\", \"is_driver\" : true }");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/30", "{\n" +
                " 'longitude': 30.0,\n" +
                " 'latitude': 30.0,\n" +
                " 'street': 'Street 30'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/31", "{\n" +
                " 'longitude': 31.0,\n" +
                " 'latitude': 31.0,\n" +
                " 'street': 'Street 31'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/32", "{\n" +
                " 'longitude': 32.0,\n" +
                " 'latitude': 32.0,\n" +
                " 'street': 'Street 32'\n" +
                "}");
        sendHTTPReq("PATCH", client, "http://localhost:8004/location/33", "{\n" +
                " 'longitude': 33.0,\n" +
                " 'latitude': 33.0,\n" +
                " 'street': 'Street 33'\n" +
                "}");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 30\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 31\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 32\", \"hasTraffic\" : false }");
        sendPutReq(client, "http://localhost:8004/location/road", "{ \"roadName\": \"Street 33\", \"hasTraffic\" : false }");

        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 30\", \"roadName2\": \"Street 31\", \"hasTraffic\" : true, \"time\": 3 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 31\", \"roadName2\": \"Street 32\", \"hasTraffic\" : true, \"time\": 4 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 32\", \"roadName2\": \"Street 33\", \"hasTraffic\" : true, \"time\": 7 }");
        sendHTTPReq("POST", client, "http://localhost:8004/location/hasRoute", "{ \"roadName1\": \"Street 30\", \"roadName2\": \"Street 33\", \"hasTraffic\" : true, \"time\": 50 }");

        HttpResponse response1 = sendPostReq(client, "http://localhost:8004/trip/confirm", "{\n" +
                "\t\"driver\": \"33\", \n" +
                "\t\"passenger\": \"30\",\n" +
                "\t\"startTime\": 1669342329\n" +
                "}");

        JSONObject obj = null;
        try {
            obj = new JSONObject(response1.body().toString());
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
        String tripId = obj.getJSONObject("_id").getString("$oid");

        sendHTTPReq("PATCH", client, "http://localhost:8004/trip/" + tripId, "{\n" +
                "\t\"distance\": 12, \n" +
                "\t\"endTime\": 14,\n" +
                "\t\"timeElapsed\": 40,\n" +
                "\t\"discount\": 12.0,\n" +
                "\t\"totalCost\": 30.0,\n" +
                "\t\"driverPayout\": 12.0\n" +
                "}");


        HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/driverTime/" + tripId);
        String receivedResponse = response.body().toString();
        String correctResponse = "{\"data\":{\"arrival_time\":14},\"status\":\"OK\"}";
        System.out.println(receivedResponse);
        assertTrue(receivedResponse.equals(correctResponse) && response.statusCode() == 200);
    }

    @Test
    public void driverTimeFail() throws URISyntaxException, IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse response = sendGetReq(client, "http://localhost:8004/trip/driverTime/2342ee2342342357hy");
        String receivedResponse = response.body().toString();
        assertTrue(receivedResponse.equals("{\"status\":\"NOT FOUND\"}") && response.statusCode() == 404);
    }
}