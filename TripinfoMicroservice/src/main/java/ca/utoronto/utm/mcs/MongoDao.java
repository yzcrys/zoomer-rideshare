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

	// *** implement database operations here *** //
	public String addTripRequest(String uid, Integer radius) throws URISyntaxException, IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = sendGetReq(client, "http://locationmicroservice:8000/location/nearbyDriver/" + uid + "?radius=" + radius);

		if (response.statusCode() != 200 || response.body() == null) {
			return Integer.toString(response.statusCode());
		}
	public void addTripConfirm(String driver, String passenger, Integer startTime) {
		collection.insertOne(new Document()
				.append("_id", new ObjectId())
				.append("driver", driver)
				.append("passenger", passenger)
				.append("startTime", startTime));
	}

	// PATCH trip/_id
	public Integer updateTripInfo(String _id, Integer distance, Long endTime, Integer timeElapsed, Integer discount, Integer totalCost, Float driverPayout) {
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

		if (dbObj.equals(null)) {
			return 400;
		}
		return 200;
	}
}
