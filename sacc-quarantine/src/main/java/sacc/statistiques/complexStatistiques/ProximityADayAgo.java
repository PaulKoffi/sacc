package sacc.statistiques.complexStatistiques;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import sacc.utils.Sha1Hash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

@WebServlet(name = "ProximityADayAgo", urlPatterns = "/last24hours")
public class ProximityADayAgo extends HttpServlet {

    private LastProximitiesSub lastProximitiesSub = new LastProximitiesSub();
    private Firestore firestoreDb;

    public ProximityADayAgo() throws IOException {
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

        Publisher publisher = null;
        String topicId = "lastPoiProximity";
        String projectId = "sacc-quarantine";
        TopicName topicName = TopicName.of(projectId, topicId);
        try {
            // create a publisher on the topic
            publisher = Publisher.newBuilder(topicName).build();
            // construct a pubsub message from the payload
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();
            ByteString data = ByteString.copyFromUtf8(payload);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            // redirect to home page
            sendAsJson(resp, resp.getStatus());
            lastProximitiesSub.subscribeAsyncExample(projectId, payload);
            //lastProximitiesSub.getUserMailsList();
            /**
             * Mettre ton code ici
             */
//            createFile2((ArrayList<String>) lastProximitiesSub.getUserMailsList());
            connectToDatabase();

            if (checkIfAdmin(payload)) {
                System.out.println("P " + payload);
                String l = createFile2((ArrayList<String>) lastProximitiesSub.getUserMailsList());
//                l = createFile((ArrayList<String>) lastProximitiesSub.getUserMailsList());
//                uplodadNewFileToCloudStorage(l.get(0), l.get(1));
                String link = "https://storage.googleapis.com/bucket_quarantine/" + l;
                String msg = "<h3>Lien vers le fichier :  <a href=" + link + ">linkToFile</a>!</h3><br />Go voir les StATS!";
                sendMail(payload, msg);
                System.out.println("DONE !");
            }



        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                try {
                    publisher.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendAsJson(HttpServletResponse response, Object obj) throws IOException {
        Gson _gson = new Gson();
        response.setContentType("application/json");
        String res = _gson.toJson(obj);
        PrintWriter out = response.getWriter();
        out.print(res);
        out.flush();
    }


    private ArrayList<String> createFile(ArrayList<String> list)
            throws IOException {

        ArrayList<String> arrlist = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
//        System.out.println(dateFormat.format(date));
        Writer fileWriter = new FileWriter("src/main/java/sacc/resources/" + "STATS_" + dateFormat.format(date).replaceAll("\\s+", "") + ".txt", false); //overwrites file
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/sacc/resources/" + "STATS_" + dateFormat.format(date).replaceAll("\\s+", "") + ".txt", true));

        writer.append("Personnes ayant été en contact avec une PoI au cours des dernières 24h :");
        writer.append("\n");
        for (int counter = 0; counter < list.size(); counter++) {
//            System.out.println(arrlist.get(counter));
            writer.append(list.get(counter));
            writer.append("\n");
        }
        writer.append("__________________FIN_________________________");
        writer.close();
        arrlist.add("src/main/java/sacc/resources/" + "STATS_" + dateFormat.format(date).replaceAll("\\s+", "") + ".txt");
        arrlist.add("STATS_" + dateFormat.format(date).replaceAll("\\s+", "") + ".txt");
        return arrlist;
    }

    private String createFile2(ArrayList<String> list)
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

//    private void uplodadNewFileToCloudStorage(String path, String name) throws IOException {
//        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
//        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//        BlobId blobId = BlobId.of("bucket_quarantine", name);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
//        System.out.println("Doneeeeeeeeeeee !!!!!!!!!!!!!!!!!!");
//        Path pathToFile = Paths.get(path);
//        storage.create(blobInfo, Files.readAllBytes(pathToFile));
//    }

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

    private boolean checkIfAdmin(String emailAdmin) {
        DocumentReference docRefAdmin = firestoreDb.collection("admins").document(Sha1Hash.encryptThisString(emailAdmin));
        ApiFuture<DocumentSnapshot> future = docRefAdmin.get();
        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(document != null);
        return document != null;
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
}
