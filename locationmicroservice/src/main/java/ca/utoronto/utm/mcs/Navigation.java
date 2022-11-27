package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {

        System.out.println("GET NAVIGATIONAA");

        String[] split1 = r.getRequestURI().toString().split("/");

        if (split1.length != 4 || split1[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String[] params = split1[3].split("\\?passengerUid=");
        if (params.length != 2 || params[1].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String passengerUid = params[0];
            String driverUid = params[1];

            Result result = this.dao.getNavigation(driverUid, passengerUid);

            if (result.hasNext()){
                Record record = result.next();

                Value time = record.get("time");
                Path shortestPath = record.get("shortestPath").asPath();

                Iterable<Node> nodes = shortestPath.nodes();
                Iterable<Relationship> relationships = shortestPath.relationships();

                ArrayList<Node> nodeList = new ArrayList<Node>();
                ArrayList<Relationship> relList = new ArrayList<Relationship>();
                ArrayList<JSONObject> pathToDestination = new ArrayList<JSONObject>();

                for (Node node : nodes) nodeList.add(node);

                for (Relationship relationship : relationships) relList.add(relationship);

                for (int i = 0; i < nodeList.size(); i++) {
                    JSONObject streetName = new JSONObject();
                    streetName.put("street", nodeList.get(i).asMap().get("name"));

                    if (i == 0) {
                        streetName.put("time", 0);
                        streetName.put("is_traffic", relList.get(i).asMap().get("has_traffic"));
                    }
                    else {
                        streetName.put("time", relList.get(i - 1).asMap().get("travel_time"));
                        streetName.put("is_traffic", relList.get(i - 1).asMap().get("has_traffic"));
                    }

                    pathToDestination.add(streetName);
                }

//
//                System.out.println("AJSDFNAJKSFN " + shortestPath.end().asMap().get("properties").toString());
//                JSONObject streetName = new JSONObject();
//                streetName.put("street", nodeList.get(i).asMap().get("name"));

                JSONObject data = new JSONObject();
                data.put("total_time", time);
                data.put("route", pathToDestination);

                JSONObject res = new JSONObject();
                res.put("status", "OK");
                res.put("data", data);
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
