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
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import sacc.models.Statistique;
import sacc.utils.Sha1Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NumberOfPositionChangementSub {

    private Firestore firestoreDb;
    private int numberOfPositionChangement = 0;

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

    public void subscribeAsyncExample(String projectId) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, "numberOfPositionChangementStatsSub");

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
                                List<Integer> changements = new ArrayList<>();
                                Map<String, Object> map = document.getData();
                                assert map != null;
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if(entry.getKey().equals("numberOfModification")){
                                        changements.add(Math.toIntExact((Long) entry.getValue()));
                                    }
                                }
                                for (int b:changements) {
                                    numberOfPositionChangement+=b;
                                }
                            } else {
                                System.out.println("No such document!");
                            }
                        });
                        Statistique statistique = new Statistique("number of changement of position", numberOfPositionChangement);
                        System.out.println(numberOfPositionChangement);

                        // Sending Mail
                        String msg = "<h3>Nombre de personnes ayant changer de positions  : </h3><br />"+numberOfPositionChangement;
                        sendMail(message.getData().toStringUtf8(), msg);
                        System.out.println("DONEEEEEEEEE !!!");
                    }
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();
                // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
                subscriber.awaitTerminated(10, TimeUnit.SECONDS);
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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert document != null;
        if (document.exists()) {
            return true;
        } else {
            System.out.println("No such document!");
        }
        return false;
    }


    private void sendMail(String adminMail, String htmlMsg) {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;
        client = new MailjetClient("381049ba9918bba1264fa0a8885d53ae", "2d5024c1a1cdbf4d89ec7690c3a982d5", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "cloudComputingWakandaGroup@paulkoffi.com")
                                        .put("Name", "WakandaGroup"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", adminMail)
                                                .put("Name", "Admin")))
                                .put(Emailv31.Message.SUBJECT, "STATISTIQUES SUR LE NOMBRE DE PERSONNES AYANT CHANGER DE POSITION")
                                .put(Emailv31.Message.TEXTPART, "stats")
                                .put(Emailv31.Message.HTMLPART, htmlMsg)
                                .put(Emailv31.Message.CUSTOMID, "StatsMail")));
        try {
            response = client.post(request);
            System.out.println(response.getStatus());
            System.out.println(response.getData());
            System.out.println("yes");
        } catch (MailjetException e) {
            e.printStackTrace();
        } catch (MailjetSocketTimeoutException e) {
            e.printStackTrace();
        }
    }
}
