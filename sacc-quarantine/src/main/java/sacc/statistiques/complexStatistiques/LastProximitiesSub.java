package sacc.statistiques.complexStatistiques;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LastProximitiesSub {

    private Firestore firestoreDb;

    void subscribeAsyncExample(String projectId) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, "lastPoiProximityStats");

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println("Id: " + message.getMessageId());
                    System.out.println("Data: " + message.getData().toStringUtf8());
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();
            System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());
            filterByDate();
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            subscriber.awaitTerminated(10, TimeUnit.SECONDS);
        } catch (TimeoutException | IOException timeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber.stopAsync();
        }
    }

    private List<DocumentReference> filterByDate() throws IOException {
        List<DocumentReference> documentReferences = new ArrayList<>();
        connectToDatabase();
        Iterable<DocumentReference> docRef = firestoreDb.collection("proximity").listDocuments();
        docRef.forEach(documentReference -> {
        ApiFuture<DocumentSnapshot> future = documentReference.get();
// ...
// future.get() blocks on response
        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert document != null;
        if (document.exists()) {
            System.out.println("Document data: " + document.getData());
            Map<String, Object> map = document.getData();
            assert map != null;
            String date = (String)map.get("date");
            try {
                if(last24hours(date)){
                    documentReferences.add(documentReference);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No such document!");
        }
        for(DocumentReference doc:documentReferences){
            System.out.println(doc);
        }
        });
// asynchronously retrieve the document
        //ApiFuture<DocumentSnapshot> future = docRef.get();
// ...
// future.get() blocks on response
        /*DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert document != null;
        if (document.exists()) {
            System.out.println("Document data: " + document.getData());
            /*Map<String, Object> map = document.getData();
            assert map != null;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if(entry.getValue())
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }
        } else {
            System.out.println("No such document!");
        }*/

        return documentReferences;
    }

    private void connectToDatabase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setProjectId("sacc-quarantine")
                    .build();
            FirebaseApp.initializeApp(options);
        }else{
            FirebaseApp.getInstance();
        }
        firestoreDb = FirestoreClient.getFirestore();
    }

    private boolean last24hours(String date) throws ParseException {
        SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss");
        Date lFromDate1 = datetimeFormatter1.parse(date);
        Long now = new Date().getTime();
        Long proximitydate = lFromDate1.getTime();
        return now - proximitydate < 86400000;
    }
}
