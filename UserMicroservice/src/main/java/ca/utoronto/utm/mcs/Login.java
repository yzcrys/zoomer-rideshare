package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            JSONObject body = null;
            try {
                body = new JSONObject(Utils.convert(r.getRequestBody()));
            } catch (JSONException e) {
                e.printStackTrace();
                if (body == null) {
                    this.sendStatus(r, 400);
                    return;
                }
//                System.out.println(body.toString());
                this.sendStatus(r, 400);
                return;
            }

            String fields[] = {"email", "password"};

            Class<?> fieldClasses[] = {String.class, String.class};
            if (!validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }

            String email = body.getString("email");
            String password = body.getString("password");

            // make query and get required data, return 500 if error
            ResultSet rs;
            boolean resultHasNext;
            try {
                rs = this.dao.loginUser(email, password);
                resultHasNext = rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            if (!resultHasNext) {
                this.sendStatus(r, 404);
                return;
            }

            try {
                if (rs.getString("password").equals(password))
                    this.sendStatus(r, 200);
                else
                    this.sendStatus(r, 401);
            } catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }
        }catch (Exception e) {
            this.sendStatus(r, 500);
        }
    }
}
