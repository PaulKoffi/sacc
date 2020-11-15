package sacc.proximityMessage;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import sacc.models.Proximity;
import sacc.utils.Sha1Hash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static sacc.mocks.Statistiques.incrementNumberOfUser;

@WebServlet(name = "ProximityMsg",
        urlPatterns = "/sendProximityMsg")
public class ProximityMsg extends HttpServlet {
    Firestore db;
    private Gson _gson = null;

    public ProximityMsg() throws IOException {
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

        Proximity proximity = gson.fromJson(payload, Proximity.class);

        String id = Sha1Hash.encryptThisString(proximity.getUser1PhoneNumber()+proximity.getUser2PhoneNumber());

        DocumentReference docRefFirstUser = db.collection("users").document(Sha1Hash.encryptThisString(proximity.getUser1PhoneNumber()));

        ApiFuture<DocumentSnapshot> future = docRefFirstUser.get();

        DocumentSnapshot documentFistUser = null;
        try {
            documentFistUser = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        DocumentReference docRefSecondUser = db.collection("users").document(Sha1Hash.encryptThisString(proximity.getUser2PhoneNumber()));
        ApiFuture<DocumentSnapshot> future2 = docRefSecondUser.get();
        DocumentSnapshot documentSecondUser = null;
        try {
            documentSecondUser = future2.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        assert documentFistUser != null;
        if (documentFistUser.exists()) {
            assert documentSecondUser != null;
            if (documentSecondUser.exists()) {

                DocumentReference docRef = db.collection("proximity").document(Sha1Hash.encryptThisString(id));
                Map<String, Object> data = new HashMap<>();
                data.put("user1CurrentLocation", proximity.getUser1CurrentLocation());
                data.put("User2CurrentLocation", proximity.getUser2CurrentLocation());
                data.put("user1PhoneNumber", Sha1Hash.encryptThisString(proximity.getUser1PhoneNumber()));
                data.put("user2PhoneNumber", Sha1Hash.encryptThisString(proximity.getUser2PhoneNumber()));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                data.put("date", formatter.format(date));

                ApiFuture<WriteResult> result = docRef.set(data);
            }
        }
    }

}
