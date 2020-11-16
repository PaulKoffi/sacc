package sacc.statistiques.basicsStatistiques.subscribers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NumberOfPositionChangementSub {

    private Firestore firestoreDb;

    public NumberOfPositionChangementSub() throws IOException {
        connectToDatabase();
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

    void subscribeAsyncExample(String projectId, String emailAdmin) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, "lastPoiProximityStats");

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println("Id: " + message.getMessageId());
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();
            if (true){
                /*List<DocumentReference> docs = filterByDate();
                Set<String> usersMail = checkProximity(docs);
                userMailsList.addAll(usersMail);*/
                /**
                 * c'est ici que tu dois copier le truc dans le fichier dans une fonction
                 * pour après upload dans le cloudsotre
                 */
                // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
                subscriber.awaitTerminated(10, TimeUnit.SECONDS);
            }
        } catch (TimeoutException timeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber.stopAsync();
        }
    }
}
