package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Driver extends Endpoint {

    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try{
            String[] split1 = r.getRequestURI().getPath().split("/");
            JSONArray res;

            if (split1.length != 4 | split1[3].equals("")) {
                this.sendStatus(r, 400);
                return;
            }

            String uidString = split1[3];

            res = this.dao.getAllTrips(uidString);

            if(0 == res.length()){
                this.sendStatus(r, 404);
                return;
            }

            JSONObject rData = new JSONObject();
            JSONObject trips = new JSONObject();

            trips.put("trips", res);
            rData.put("data", trips);

            this.sendResponse(r, rData, 200);
            return;
        }catch(Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
