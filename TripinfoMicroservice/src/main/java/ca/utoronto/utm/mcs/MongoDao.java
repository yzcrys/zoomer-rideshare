package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.sql.Time;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

public class MongoDao {
	
	public MongoCollection<Document> collection;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.

//		String uri = "mongodb://root:123456@" + System.getenv("MONGODB_ADDR") + ":27017/?maxPoolSize=20&w=majority";
		String connectionString = "mongodb://root:123456@" + System.getenv("MONGODB_ADDR") + ":27017/root?&ssl=false";
		System.out.println("the conn string: " + connectionString);

		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoDatabase database = mongoClient.getDatabase("trip");
			collection = database.getCollection("trips");

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

	// *** implement database operations here *** //

	public void addTripRequest(String uid, Integer radius) {
		//

		System.out.println("Success! Inserted document");
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
