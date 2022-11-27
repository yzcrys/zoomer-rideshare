package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mongodb.client.model.Filters.eq;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class MongoDao {

	public MongoCollection<Document> collection;
	private final MongoClient mongoClient;
	private final ClientSession session;
	private final MongoDatabase database;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.

		String uri = "mongodb://root:123456@host.docker.internal:27017";

		MongoClient mongoClient = MongoClients.create(uri);
		this.mongoClient = MongoClients.create(uri);
		this.session = mongoClient.startSession();
		this.database = mongoClient.getDatabase("trips");
		this.collection = this.database.getCollection("trips");
	}

	public HttpResponse<String> sendGetReq(HttpClient client, String uriEndpoint) throws URISyntaxException, IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder()
				.uri(new URI(uriEndpoint))
				.method("GET", noBody())
				.build(), HttpResponse.BodyHandlers.ofString());
	}


	public boolean checkTripExists(ObjectId _id){
		FindIterable<Document> res = collection.find(Filters.eq("_id", _id));

		try {
			JSONArray result = new JSONArray();
			for (Document doc : res) {
				doc.put("_id", doc.getObjectId("_id").toString());
				result.put(doc);
			}
			if(result.length() == 0){
				return false;
			}
			return true;
		}catch (Exception e) {
			throw e;
		}
	}

	// *** implement database operations here *** //
	public String addTripRequest(String uid, Integer radius) throws URISyntaxException, IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = null;
		try {
			response = sendGetReq(client, "http://locationmicroservice:8000/location/nearbyDriver/" + uid + "?radius=" + radius);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "500";
		}

		if (response.statusCode() != 200 || response.body() == null) {
			return Integer.toString(response.statusCode());
		}
		return response.body();
	}

	public Document addTripConfirm(String driver, String passenger, Integer startTime) {

		Document doc = new Document()
				.append("_id", new ObjectId())
				.append("driver", driver)
				.append("passenger", passenger)
				.append("startTime", startTime);
		collection.insertOne(doc);
		return doc;
	}

	// PATCH trip/_id
	public int updateTripInfo(String _id, Integer distance, Integer endTime, Integer timeElapsed, Integer discount, Double totalCost, Double driverPayout) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		query.put("_id", new ObjectId(_id));
		update.put("distance", distance);
		update.put("endTime", endTime);
		update.put("timeElapsed", timeElapsed);
		update.put("discount", discount);
		update.put("totalCost", totalCost);
		update.put("driverPayout", driverPayout);

		DBObject dbObj = (DBObject) collection.findOneAndUpdate(query, update);

		if (dbObj == null || dbObj.equals(null)) {
			return 404;
		}
		return 200;
	}

	public JSONArray getAllTrips(String uid) {

		FindIterable<Document> found = this.collection.find(Filters.eq("driver", uid));
		try {
			JSONArray res = new JSONArray();

			for (Document doc : found) {
				doc.put("_id",doc.getObjectId("_id").toString());
				doc.remove("driver");
				res.put(doc);
			}
			return res;
		} catch (Exception e) {
			throw e;
		}
	}
}
