package sacc.statistiques.complexStatistiques;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import sacc.utils.Sha1Hash;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Properties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class LastProximitiesSub {

    private Firestore firestoreDb;
    private List<String> userMailsList;

    LastProximitiesSub() throws IOException {
        connectToDatabase();
    }

    void subscribeAsyncExample(String projectId) {
        this.userMailsList = new ArrayList<>();
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, "lastPoiProximityStats");

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println(message.getData().toStringUtf8());
                    System.out.println("Id: " + message.getMessageId());
                    if (checkIfAdmin(message.getData().toStringUtf8())) {
                        List<DocumentReference> docs = null;
                        try {
                            docs = filterByDate();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Set<String> usersMail = checkProximity(Objects.requireNonNull(docs));
                        userMailsList.addAll(usersMail);


//                        UPLOAD FILE
                        System.out.println("Mail " + message.getData().toStringUtf8());
                        try {
                            String l = createAndUpload((ArrayList<String>) userMailsList);
                            String link = "https://storage.googleapis.com/bucket_quarantine/" + l;
                            String msg = "<h3>Lien vers le fichier :  <a href=" + link + ">linkToFile</a>!</h3><br />Go voir les StATS!";
                            sendMail(message.getData().toStringUtf8(), msg);
                            System.out.println("DONEEEEEEEEE !!!");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();

            /**
             * c'est ici que tu dois copier le truc dans le fichier dans une fonction
             * pour après upload dans le cloudsotre
             */
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            subscriber.awaitTerminated(10, TimeUnit.SECONDS);

        } catch (TimeoutException timeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber.stopAsync();
        }
    }

    private List<DocumentReference> filterByDate() throws IOException {
        List<DocumentReference> documentReferences = new ArrayList<>();
        //connectToDatabase();
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
                Map<String, Object> map = document.getData();
                assert map != null;
                String date = (String) map.get("date");
                try {
                    if (last24hours(date)) {
                        documentReferences.add(documentReference);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No such document!");
            }
        });
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
        } else {
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

    private Set<String> checkProximity(List<DocumentReference> documentReferencesfilteredByDate) {
        Set<String> emails = new HashSet<>();
        documentReferencesfilteredByDate.forEach(documentReference -> {
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
                Map<String, Object> map = document.getData();
                assert map != null;
                String user1Sha1 = (String) map.get("user1PhoneNumber");
                String user2Sha1 = (String) map.get("user2PhoneNumber");
                try {
                    boolean val = booleanIfPOI(user1Sha1);
                    if (val) {
                        emails.add(emailUser(user2Sha1));
                    } else {
                        val = booleanIfPOI(user2Sha1);
                        if (val) {
                            emails.add(emailUser(user1Sha1));
                        }
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No such document!");
            }
        });
        for (String mail : emails) {
            System.out.println(mail);
        }
        return emails;
    }

    private boolean booleanIfPOI(String sha1) throws ExecutionException, InterruptedException {
        //asynchronously retrieve multiple documents
        ApiFuture<QuerySnapshot> future =
                firestoreDb.collection("users").whereEqualTo("phoneNumber", sha1).get();
// future.get() blocks on response
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (DocumentSnapshot document : documents) {
            Map<String, Object> map = document.getData();
            assert map != null;
            return (Boolean) map.get("personOfInterest");
        }
        return false;
    }

    private String emailUser(String sha1) throws ExecutionException, InterruptedException {
        //asynchronously retrieve multiple documents
        ApiFuture<QuerySnapshot> future =
                firestoreDb.collection("users").whereEqualTo("phoneNumber", sha1).get();
// future.get() blocks on response
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (DocumentSnapshot document : documents) {
            Map<String, Object> map = document.getData();
            assert map != null;
            return (String) map.get("email");
        }
        return "";
    }

    public List<String> getUserMailsList() {
        return userMailsList;
    }

    private boolean checkIfAdmin(String emailAdmin) {

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

    private String createAndUpload(ArrayList<String> list)
            throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String t = "STATS_" + dateFormat.format(date).replaceAll("\\s+", "");
        // Create a temp file to upload
        Path tempPath = Files.createTempFile(t, "txt");
        String r = "Personnes ayant été en contact avec une PoI au cours des dernières 24h :" + "\n";
        for (int counter = 0; counter < list.size(); counter++) {
            r = r + list.get(counter) + "\n";
        }
        r = r + "__________________FIN_________________________";
        Files.write(tempPath, r.getBytes());
        File tempFile = tempPath.toFile();
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        BlobId blobId = BlobId.of("bucket_quarantine", t);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        System.out.println("Doneeeeeeeeeeee !!!!!!!!!!!!!!!!!!");
        storage.create(blobInfo, Files.readAllBytes(tempPath));
        tempFile.deleteOnExit();
        return t;
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
                                .put(Emailv31.Message.SUBJECT, "STATISTIQUES GLOBALES")
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
