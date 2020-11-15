package sacc.proximityMessage;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import sacc.models.User;
import sacc.utils.Sha1Hash;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

        User user = gson.fromJson(payload, User.class);

        String phoneNumber = user.getNumber();

        DocumentReference docRef = db.collection("users").document(Sha1Hash.encryptThisString(phoneNumber));
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("personOfInterest", user.getPersonOfInterest());
        data.put("phoneNumber", Sha1Hash.encryptThisString(phoneNumber));

        ApiFuture<WriteResult> result = docRef.set(data);
        incrementNumberOfUser();
    }

}
