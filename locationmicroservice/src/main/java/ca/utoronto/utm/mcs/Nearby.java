package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {

        System.out.println("NEARBYNEARBYNEARBY bink NEARBY NEARBY");

        System.out.printf("%s", r.getRequestURI().toString().split("/")[3]);

        String[] split1 = r.getRequestURI().toString().split("/");
        if (split1.length != 4 || split1[3].isEmpty()) {
            System.out.printf("SPLIT1a: ", split1[3]);
            this.sendStatus(r, 400);
            return;
        }

        System.out.printf("SPLIT1b: ", split1[3]);

        String[] params = split1[3].split("\\?radius=");
        if (params.length != 2 || params[1].isEmpty()) {
            System.out.printf("PARAMSa: ", params[0]);
            this.sendStatus(r, 400);
            return;
        }

        try {
            String uid = params[0];
            Integer radius = Integer.parseInt(params[1]);

            System.out.printf("Params: %s, %d", uid, radius);
            Result drivers = this.dao.getNearbyDriver(uid, radius);
            if (drivers.hasNext()) {
                JSONObject result = new JSONObject();
                while (drivers.hasNext()) {
                    Record potentialDriver = drivers.next();
                    Value driver = potentialDriver.get("driver");

                    JSONObject driverData = new JSONObject();

                    driverData.put("longitude", driver.asMap().get("longitude"));
                    driverData.put("latitude", driver.asMap().get("latitude"));
                    driverData.put("street", driver.asMap().get("street"));
                    result.put((String)driver.asMap().get("uid"), driverData);
                }
                JSONObject res = new JSONObject();
                res.put("data", result);
                res.put("status", "OK");

                this.sendResponse(r, res, 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
