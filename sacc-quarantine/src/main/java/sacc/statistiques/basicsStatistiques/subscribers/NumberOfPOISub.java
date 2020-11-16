package sacc.statistiques.basicsStatistiques.subscribers;

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
import sacc.models.Statistique;
import sacc.utils.Sha1Hash;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NumberOfPOISub {
    private Firestore firestoreDb;
    private int numberOfPOI;

    public NumberOfPOISub() throws IOException {
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

    public void subscribeAsyncExample(String projectId) {
        numberOfPOI = 0;
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, "numberOfPOIStatsSub");

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println("Id: " + message.getMessageId());
                    if (checkIfAdmin(message.getData().toStringUtf8())){
                        Iterable<DocumentReference> docRef = firestoreDb.collection("users").listDocuments();
                        docRef.forEach(documentReference -> {
                            ApiFuture<DocumentSnapshot> future = documentReference.get();
                            DocumentSnapshot document = null;
                            try {
                                document = future.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            assert document != null;
                            if (document.exists()) {
                                List<Boolean> peopleOfInterest = new ArrayList<>();
                                Map<String, Object> map = document.getData();
                                assert map != null;
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if(entry.getKey().equals("personOfInterest")){
                                        peopleOfInterest.add((Boolean) entry.getValue());
                                    }
                                }
                                for (Boolean b:peopleOfInterest) {
                                    if (b){
                                        numberOfPOI++;
                                    }
                                }
                            } else {
                                System.out.println("No such document!");
                            }
                        });
                        Statistique statistique = new Statistique("number of POI", numberOfPOI);
                        System.out.println(numberOfPOI);
                    }
                    /**
                     * c'est ici que tu dois copier le truc dans le fichier dans une fonction
                     * pour après upload dans le cloudsotre
                     */
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();

                // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
                subscriber.awaitTerminated(20, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber.stopAsync();
        }
    }

    private boolean checkIfAdmin(String emailAdmin){

        DocumentReference docRefAdmin = firestoreDb.collection("admins").document(Sha1Hash.encryptThisString(emailAdmin));

        ApiFuture<DocumentSnapshot> future = docRefAdmin.get();

        DocumentSnapshot document = null;
        try {
            document = future.get();
            System.out.println(document != null);
            return document != null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
