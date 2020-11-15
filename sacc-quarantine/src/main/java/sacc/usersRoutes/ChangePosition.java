package sacc.usersRoutes;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import sacc.Location;
import sacc.models.User;
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

@WebServlet(name = "changePosition",
        urlPatterns = "/changePosition"
)
public class ChangePosition  extends HttpServlet {

    Firestore db;
    private Gson _gson = null;

    public ChangePosition() throws IOException {
        connectToDatabase();
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
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
        Location location= user.getLocation();
        int updateNumber = user.getNumberOfModification();
        Map<String, Object> data = new HashMap<>();
        Date date = new Date(System.currentTimeMillis());
        data.put("numberOfModification", FieldValue.increment(1));
        data.put("location", location);


        ApiFuture<WriteResult> writeResult = db.collection("users")
                .document(Sha1Hash.encryptThisString(phoneNumber))
                .set(data, SetOptions.merge());
//                .collection("users").document(Sha1Hash.encryptThisString(phoneNumber).set(data, SetOptions.merge());


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
        db = FirestoreClient.getFirestore();
    }
}
