package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import java.io.IOException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {

        try {
            System.out.println("RIGHT HERE \n \n");
            String[] split1 = r.getRequestURI().toString().split("/");
            System.out.println("\n\n" + split1[2] + ", " + split1.length + "\n\n");
            if (split1.length != 3 || split1[2].isEmpty()) {
                this.sendStatus(r, 400);
                return;
            }

            String _id = split1[2];
            String reqBody = Utils.convert(r.getRequestBody());
            if (reqBody.equals("") || !ObjectId.isValid(_id)) {
                this.sendStatus(r, 400);
                return;
            }

            JSONObject body = new JSONObject(reqBody);
            String fields[] = {"distance", "endTime", "timeElapsed", "discount", "totalCost", "driverPayout"};

            Class<?> fieldClasses[] = {Integer.class, Integer.class, Integer.class, Double.class, Double.class, Double.class};
            if (!validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }

            int distance = body.getInt("distance");
            int endTime = body.getInt("endTime");
            int timeElapsed = body.getInt("timeElapsed");
            Double discount = body.getDouble("discount");
            Double totalCost = body.getDouble("totalCost");
            Double driverPayout = body.getDouble("driverPayout");


            int doc;
            try {
                System.out.println("trydao");
                doc = dao.updateTripInfo(new ObjectId(_id), distance, endTime, timeElapsed, discount, totalCost, driverPayout);
//                System.out.println("\n" + doc);
                this.sendStatus(r, doc);
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 404);
            }
        } catch(Exception e) {
            this.sendStatus(r, 404);
        }
    }
}
