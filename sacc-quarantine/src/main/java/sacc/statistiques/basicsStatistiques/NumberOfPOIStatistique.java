package sacc.statistiques.basicsStatistiques;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import sacc.models.Statistique;
import sacc.statistiques.basicsStatistiques.subscribers.NumberOfPOISub;
import sacc.utils.Sha1Hash;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "NumberOfPOIStatistique", value = "/statistiques/numberOfPOI")
public class NumberOfPOIStatistique extends HttpServlet {

    private Gson _gson = new Gson();
    private Firestore firestoreDb;
    private int numberOfPOI = 0;
    private NumberOfPOISub numberOfPOISub = new NumberOfPOISub();
    public NumberOfPOIStatistique() throws IOException {
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Publisher publisher = null;
        String topicId = "numberOfPOIStats";
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
            sendAsJson(response,response.getStatus());
            numberOfPOISub.subscribeAsyncExample(projectId);
            //lastProximitiesSub.getUserMailsList();
            /**
             * Mettre ton code ici
             */
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

    private void sendAsJson( HttpServletResponse response, Object obj) throws IOException {
        response.setContentType("application/json");
        String res = _gson.toJson(obj);
        PrintWriter out = response.getWriter();
        out.print(res);
        out.flush();
    }
}
