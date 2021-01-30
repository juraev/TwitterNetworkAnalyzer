package models;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class TwitterRepositoryImpl implements TwitterRepository {
    private static final int PORT = 27017;
    private static final String HOST = "localhost";
    private static final String DATABASE_NAME = "twitter_connections";
    private static final String USER_COLLECTION_NAME = "twitter_users";

    MongoClient mDBClient;
    MongoDatabase mDatabase;

    public TwitterRepositoryImpl() {
        mDBClient = new MongoClient(HOST, PORT);
        mDatabase = mDBClient.getDatabase(DATABASE_NAME);
    }

    @Override
    public int addFollower(User followee, User follower) {
        MongoCollection<Document> collection = mDatabase.getCollection(USER_COLLECTION_NAME);

        Bson filter = eq("_Id", followee.getId());
        Bson update = addToSet("follower_ids", follower.mId);

        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult result = collection.updateOne(filter, update, options);
        System.out.println(result);

        return 1;
    }

    @Override
    public int addFollowers(User followee, List<User> followers) {
        MongoCollection<Document> collection = mDatabase.getCollection(USER_COLLECTION_NAME);
        Bson filter1 = eq("_id", followee.getId());
        Bson filter2 = eq("name", followee.getName());
        Bson filter3 = eq("username", followee.getUserName());
        Bson filter = combine(filter1, filter2, filter3);
        Bson update = addEachToSet("follower_ids", Arrays.asList(followers.stream().map(u -> u.mId).toArray()));
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult result = collection.updateOne(filter, update, options);
        System.out.println(result);
        return 1;
    }

    @Override
    public void close() {
        if (mDBClient != null) mDBClient.close();
    }
}
