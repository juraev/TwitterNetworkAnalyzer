package Experiment;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static java.util.Arrays.asList;

public class ConnectToDB {
    private static final Random rand = new Random();

    public static void main(String args[]) {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("twitter_api.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String bearer = properties.getProperty("twitter_bearer");
        System.out.println(bearer);

//        delete();
    }

    private static Document generateNewGrade(double studentId, double classId) {
        List<Document> scores = asList(new Document("type", "exam").append("score", rand.nextDouble() * 100),
                new Document("type", "quiz").append("score", rand.nextDouble() * 100),
                new Document("type", "homework").append("score", rand.nextDouble() * 100),
                new Document("type", "homework").append("score", rand.nextDouble() * 100));
        return new Document("_id", new ObjectId()).append("student_id", studentId)
                .append("class_id", classId)
                .append("scores", scores);
    }

    private static void create() {
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            List<Document> grades = new ArrayList<>();
            for (double classId = 1d; classId <= 10d; classId++) {
                grades.add(generateNewGrade(10001d, classId));
            }

            gradesCollection.insertMany(grades);
        }
    }

    private static void read() {
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            // find one document with new Document
            Document student1 = gradesCollection.find(new Document("student_id", 10000)).first();
            System.out.println("Student 1: " + student1.toJson());

            // find one document with Filters.eq()
            Document student2 = gradesCollection.find(eq("student_id", 10000)).first();
            System.out.println("Student 2: " + student2.toJson());

            // find a list of documents and iterate throw it using an iterator.
            FindIterable<Document> iterable = gradesCollection.find(gte("student_id", 10000));
            MongoCursor<Document> cursor = iterable.iterator();
            System.out.println("Student list with a cursor: ");
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }

            // find a list of documents and use a List object instead of an iterator
            List<Document> studentList = gradesCollection.find(gte("student_id", 10000)).into(new ArrayList<>());
            System.out.println("Student list with an ArrayList:");
            for (Document student : studentList) {
                System.out.println(student.toJson());
            }

            // find a list of documents and print using a consumer
            System.out.println("Student list using a Consumer:");
            Consumer<Document> printConsumer = document -> System.out.println(document.toJson());
            gradesCollection.find(gte("student_id", 10000)).forEach(printConsumer);

            // find a list of documents with sort, skip, limit and projection
            List<Document> docs = gradesCollection.find(and(eq("student_id", 10001), lte("class_id", 5)))
                    .projection(fields(excludeId(), include("class_id", "student_id")))
                    .sort(descending("class_id"))
                    .skip(2)
                    .limit(2)
                    .into(new ArrayList<>());

            System.out.println("Student sorted, skipped, limited and projected: ");
            for (Document student : docs) {
                System.out.println(student.toJson());
            }

        }
    }

    private static void update() {
        JsonWriterSettings prettyPrint = JsonWriterSettings.builder().indent(true).build();
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            // update one document
            Bson filter = eq("student_id", 10000);
            Bson updateOperation = set("comment", "You should learn MongoDB!");
            UpdateResult updateResult = gradesCollection.updateOne(filter, updateOperation);
            System.out.println("=> Updating the doc with {\"student_id\":10000}. Adding comment.");
            System.out.println(gradesCollection.find(filter).first().toJson(prettyPrint));
            System.out.println(updateResult);

            filter = eq("student_id", 10001);
            updateResult = gradesCollection.updateMany(filter, updateOperation);
            System.out.println("\n=> Updating all the documents with {\"student_id\":10001}.");
            System.out.println(updateResult);
        }
    }

    private static void upsert() {
        JsonWriterSettings prettyPrint = JsonWriterSettings.builder().indent(true).build();
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            // update one document
            Bson filter = and(eq("student_id", 10002d), eq("class_id", 10d));
            Bson updateOperation = push("comments", "You will learn a lot if you read the MongoDB blog!");
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult updateResult = gradesCollection.updateOne(filter, updateOperation, options);
            System.out.println("\n=> Upsert document with {\"student_id\":10002.0, \"class_id\": 10.0} because it doesn't exist yet.");
            System.out.println(updateResult);
            System.out.println(gradesCollection.find(filter).first().toJson(prettyPrint));
        }
    }

    private static void findOneAndUpdate() {
        JsonWriterSettings prettyPrint = JsonWriterSettings.builder().indent(true).build();
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            // findOneAndUpdate
            Bson filter = eq("student_id", 10000);
            Bson update1 = inc("x", 10); // increment x by 10. As x doesn't exist yet, x=10.
            Bson update2 = rename("class_id", "new_class_id"); // rename variable "class_id" in "new_class_id".
            Bson update3 = mul("scores.0.score", 2); // multiply the first score in the array by 2.
            Bson update4 = addToSet("comments", "This comment is uniq"); // creating an array with a comment.
            Bson update5 = addToSet("comments", "This comment is uniq"); // using addToSet so no effect.
            Bson updates = combine(update1, update2, update3, update4, update5);
            // returns the old version of the document before the update.
            Document oldVersion = gradesCollection.findOneAndUpdate(filter, updates);
            System.out.println("\n=> FindOneAndUpdate operation. Printing the old version by default:");
            System.out.println(oldVersion.toJson(prettyPrint));

            // but I can also request the new version
            filter = eq("student_id", 10001);
            FindOneAndUpdateOptions optionAfter = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
            Document newVersion = gradesCollection.findOneAndUpdate(filter, updates, optionAfter);
            System.out.println("\n=> FindOneAndUpdate operation. But we can also ask for the new version of the doc:");
            System.out.println(newVersion.toJson(prettyPrint));
        }
    }

    private static void delete() {
        JsonWriterSettings prettyPrint = JsonWriterSettings.builder().indent(true).build();
        try (MongoClient mongoClient = new MongoClient("localhost", 27017)) {
            MongoDatabase mdb = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = mdb.getCollection("grades");

            Bson filter = eq("student_id", 10000);

            DeleteResult res = gradesCollection.deleteOne(filter);

            System.out.println(res);
        }
    }

//    public List<Integer> findSmallestSetOfVertices(int n, List<List<Integer>> edges) {
//        List<Integer> result = new ArrayList<Integer>();
//
//        List<Integer> list = edges.stream().map(edge -> edge.get(1)).distinct().collect(Collectors.toList());
//
//        Iterator<Integer> it = list.iterator();
//
//        Map<Integer, Integer> mp = new TreeMap<>();
//
//        int i = 1;
//        int cur = 0;
//        while(i <= n){
//            if(!it.hasNext()) result.add(i);
//            else if(cur == i){
//                cur = it.next();
//            } else {
//                result.add(i);
//            }
//            i ++;
//        }
//
//        return result;
//    }

}