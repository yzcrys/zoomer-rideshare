package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.Result;
import java.io.IOException;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {

        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"uid", "radius"};
        Class<?> fieldClasses[] = {String.class, Integer.class};
        if (!validateFields(body, fields, fieldClasses) || body.getInt("radius") < 0) {
            this.sendStatus(r, 400);
            return;
        }

        String uid = body.getString("uid");
        Integer radius = body.getInt("radius");

        try {
            JSONObject obj = dao.addTripRequest(uid, radius);

            if (obj == null)
                this.sendStatus(r, 400);
            else
                this.sendStatus(r, 200);
        } catch (Exception e) {

            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
