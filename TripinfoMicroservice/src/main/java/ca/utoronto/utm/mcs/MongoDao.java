package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

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

	// *** implement database operations here *** //
	public String addTripRequest(String uid, Integer radius) throws URISyntaxException, IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = sendGetReq(client, "http://locationmicroservice:8000/location/nearbyDriver/" + uid + "?radius=" + radius);

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
