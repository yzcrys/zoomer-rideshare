package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class MongoDao {

	public MongoCollection<Document> collection;
	public MongoClient mongoClient;
	public ClientSession session;
	public MongoDatabase database;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.

//		String uri = "mongodb://root:123456@" + System.getenv("MONGODB_ADDR") + ":27017/?maxPoolSize=20&w=majority";
		String uri = "mongodb://root:123456@host.docker.internal:27017";
		System.out.println("the conn string: " + uri);

		try (MongoClient mongoClient = MongoClients.create(uri)) {
			this.mongoClient = MongoClients.create(uri);
			this.session = mongoClient.startSession();
			this.database = mongoClient.getDatabase("trips");
			this.collection = this.database.getCollection("trips");

//			try {
//				Bson command = new BsonDocument("ping", new BsonInt64(1));
//				Document commandResult = database.runCommand(command);
//				System.out.println("Connected successfully to server.");
//			} catch (MongoException me) {
//				System.err.println("An error occurred while attempting to run a command: " + me);
//			}
		}catch (MongoException me) {
				System.err.println("An error occurred" + me);
			}

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
	public void addTripConfirm(String driver, String passenger, Integer startTime) {
		collection.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("driver", driver)
				.append("passenger", passenger)
				.append("startTime", startTime);

		try {
			this.collection.insertOne(doc);
			return doc;
		} catch (Exception e) {
			throw  e;
		}
	}

	// PATCH trip/_id
	public int updateTripInfo(ObjectId _id, Integer distance, Integer endTime, Integer timeElapsed, Double discount, Double totalCost, Double driverPayout) {
		Document doc = new Document()
				.append("distance", distance)
				.append("endTime", endTime)
				.append("timeElapsed", timeElapsed)
				.append("discount", discount)
				.append("totalCost", totalCost)
				.append("driverPayout", driverPayout);

		try{
			if(!checkTripExists(_id)){
				return 404;
			}
		}catch (Exception e) {
			throw e;
		}
		try {
			this.collection.updateOne(Filters.eq("_id", _id), new Document("$set", doc));
			return 200;
		}catch (Exception e) {
			throw e;
		}
	}

	public JSONArray getAllTrips(String uid) {

		FindIterable<Document> found = this.collection.find(Filters.eq("passenger", uid));
		try {
			JSONArray res = new JSONArray();

			for (Document doc : found) {

				doc.put("_id", doc.getObjectId("_id").toString());
				doc.remove("passenger");
				res.put(doc);
			}
			return res;
		} catch (Exception e) {
			throw e;
		}
	}
}
