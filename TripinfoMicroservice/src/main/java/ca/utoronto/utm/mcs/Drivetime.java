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

import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try{

            String[] split1 = r.getRequestURI().getPath().split("/");
            if (split1.length != 4 || split1[3].isEmpty()) {
                this.sendStatus(r, 400);
                return;
            }

            String trip_id = split1[3];
            if (!ObjectId.isValid(trip_id)) {
                this.sendStatus(r, 404);
                return;
            }

            if(!this.dao.checkTripExists(new ObjectId(trip_id))){
                this.sendStatus(r, 404);
            }

            ArrayList<String> tripUsers = this.dao.getOneTrip(new ObjectId(trip_id));
            String driver = tripUsers.get(0);
            String passenger = tripUsers.get(1);
            String objResult = "";

            objResult = dao.getDriverTime(driver, passenger);

            if (objResult.isEmpty()) {
                this.sendStatus(r, 404);
                return;
            }

            if(objResult.length() == 3){
                this.sendStatus(r, Integer.parseInt(objResult));
                return;
            }

            JSONObject response = new JSONObject(objResult);
            if (response == null) {
                this.sendStatus(r, 404);
                return;
            }
            JSONObject data = response.getJSONObject("data");
            JSONObject inner = new JSONObject();
            JSONObject outer = new JSONObject();

            inner.put("arrival_time", data.getInt("total_time"));
            outer.put("data", inner);
            this.sendResponse(r, outer, 200);

        }catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
