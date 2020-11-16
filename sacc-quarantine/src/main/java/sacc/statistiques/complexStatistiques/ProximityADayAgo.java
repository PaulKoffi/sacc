package sacc.statistiques.complexStatistiques;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import sacc.utils.Sha1Hash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@WebServlet(name="ProximityADayAgo", urlPatterns = "/last24hours")
public class ProximityADayAgo extends HttpServlet {

    private LastProximitiesSub lastProximitiesSub = new LastProximitiesSub();

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
            sendAsJson(resp,resp.getStatus());
            lastProximitiesSub.subscribeAsyncExample(projectId);
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
        Gson _gson = new Gson();
        response.setContentType("application/json");
        String res = _gson.toJson(obj);
        PrintWriter out = response.getWriter();
        out.print(res);
        out.flush();
    }
}