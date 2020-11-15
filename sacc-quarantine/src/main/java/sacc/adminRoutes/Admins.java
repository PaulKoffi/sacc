package sacc.adminRoutes;

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
import com.google.gson.GsonBuilder;
import sacc.models.Admin;
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
import java.util.concurrent.ExecutionException;

@WebServlet(name = "admin",
        urlPatterns = "/admin"
)
public class Admins extends HttpServlet {
    Firestore firestoreDb;
    private Gson _gson = null;

    public Admins() throws IOException {
        connectToDatabase();
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

        Admin admin = gson.fromJson(payload, Admin.class);

        String email = admin.getEmail();
        DocumentReference docRef = firestoreDb.collection("admins").document(Sha1Hash.encryptThisString(email));
        Map<String, Object> data = new HashMap<>();
        data.put("email", admin.getEmail());
        data.put("name", admin.getName());

        ApiFuture<WriteResult> result = docRef.set(data);
//        incrementNumberOfUser();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        DocumentReference docRef = firestoreDb.collection("admins").document(Sha1Hash.encryptThisString(email));

        ApiFuture<DocumentSnapshot> future = docRef.get();

        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert document != null;
        if (document.exists()) {
            GsonBuilder builder = new GsonBuilder();

            response.getWriter().println(document.getData());
        } else {
            System.out.println("No such document!");
        }

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
}
