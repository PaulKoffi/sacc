package sacc.usersRoutes;

import com.google.api.client.util.DateTime;
import com.google.api.core.ApiFuture;
import com.google.appengine.api.search.DateUtil;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import sacc.models.User;
import sacc.utils.Sha1Hash;
import sun.util.calendar.BaseCalendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static sacc.mocks.Statistiques.incrementNumberOfUser;

@WebServlet(name = "putUserOfInterest",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/user/putUserOfInterest")
public class PutUserOfInterest extends HttpServlet {
    Firestore db;
    private Gson _gson = null;

    public PutUserOfInterest() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("sacc-quarantine")
                .build();
        FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String payload = buffer.toString();

        Gson gson = new Gson();

        User user = gson.fromJson(payload, User.class);
        String phoneNumber = user.getNumber();
        Map<String, Object> data = new HashMap<>();
        data.put("personOfInterest", user.getPersonOfInterest());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        data.put("setAsPOIAtDate", formatter.format(LocalDate.now()));

        ApiFuture<WriteResult> writeResult = db.collection("users")
                .document(Sha1Hash.encryptThisString(phoneNumber))
                .set(data, SetOptions.merge());
//                .collection("users").document(Sha1Hash.encryptThisString(phoneNumber).set(data, SetOptions.merge());


    }
}
