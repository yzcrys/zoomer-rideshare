package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {

        System.out.println("handle register user");
        if (Utils.convert(r.getRequestBody()).isEmpty()) {
            this.sendStatus(r, 500);
            return;
        }

        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"name", "email", "password"};

        Class<?> fieldClasses[] = {String.class, String.class, String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            System.out.println("invalid fields for register user");
            this.sendStatus(r, 400);
            return;
        }

        String name = body.getString("name");
        String email = body.getString("email");
        String password = body.getString("password");

        // make query and get required data, return 500 if error
        Integer rs;
        try {
            System.out.println("try register user, " + name + ", " + password);
            rs = this.dao.registerUser(name, email, password);
        }
        catch (SQLException e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }

        this.sendStatus(r, rs);
    }
}
