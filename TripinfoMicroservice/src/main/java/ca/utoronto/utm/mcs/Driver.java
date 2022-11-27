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
            if (split1.length != 4 || split1[3].isEmpty()) {
                this.sendStatus(r, 400);
                return;
            }

            String uidString = split1[3];

            JSONArray rs;
            rs = this.dao.getAllTrips(uidString, "passenger");
            if(rs.length() == 0){
                this.sendStatus(r, 404);
                return;
            }

            JSONObject data = new JSONObject();
            JSONObject trips = new JSONObject();

            trips.put("trips", rs);
            data.put("data", trips);
            this.sendResponse(r, data, 200);
            return;

        }catch(Exception e) {
            this.sendStatus(r, 500);
        }
    }
}
