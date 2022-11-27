package ca.utoronto.utm.mcs;

import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {

        try {
            JSONObject body = null;
            try {
                body = new JSONObject(Utils.convert(r.getRequestBody()));
            } catch (JSONException e) {
                this.sendStatus(r, 400);
                return;
            }

            String fields[] = {"driver", "passenger", "startTime"};

            Class<?> fieldClasses[] = {String.class, String.class, Integer.class};
            if (!validateFields(body, fields, fieldClasses)) {
                System.out.println("in validate fields");
                this.sendStatus(r, 400);
                return;
            }

            String driver = body.getString("driver");
            String passenger = body.getString("passenger");
            Integer startTime = body.getInt("startTime");

            System.out.println("about to try confirm dao");
            Document doc;
            try {
                doc = dao.addTripConfirm(driver, passenger, startTime);
                this.sendResponse(r, new JSONObject(doc.toJson()), 200);
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
            }
        } catch (Exception e) {
            this.sendStatus(r, 500);
            return;
        }
    }
}
