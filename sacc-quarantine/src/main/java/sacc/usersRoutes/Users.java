package sacc.usersRoutes;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import sacc.models.User;
import sacc.utils.Sha1Hash;
import com.google.auth.oauth2.GoogleCredentials;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static sacc.mocks.Statistiques.incrementNumberOfUser;


@WebServlet(name = "users",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/users"
)
public class Users extends HttpServlet {
    Firestore db;
    private Gson _gson = null;

    public Users() throws IOException {
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
        if (user.getEmail() != null) data.put("email", user.getEmail());
        if (user.getPersonOfInterest()!=null) data.put("personOfInterest", user.getPersonOfInterest());
        if (!user.getNumber().equals(""))data.put("phoneNumber", Sha1Hash.encryptThisString(phoneNumber));

        ApiFuture<WriteResult> result = docRef.set(data);
        incrementNumberOfUser();
// ...
// result.get() blocks on response

        try {
            System.out.println("Update time : " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<User> users = new ArrayList<>();
        ApiFuture<QuerySnapshot> result = db.collection("users").get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            documents = result.get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert documents != null;
        for (DocumentSnapshot document : documents) {

            users.add(document.toObject(User.class));
        }
        users.forEach((User user) ->  System.out.println("============>"+ user.getEmail() + "============>"+user.getNumber() + "============>"+user.getPersonOfInterest()));
        sendAsJson(response, users);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    public static String getInfo() {
        return "Version: " + System.getProperty("java.version")
                + " OS: " + System.getProperty("os.name")
                + " User: " + System.getProperty("user.name");
    }

    private void sendAsJson(
            HttpServletResponse response,
            Object obj) throws IOException {
        response.setContentType("application/json");
        String res = _gson.toJson(obj);
        System.out.println("666666666       "+res);
        PrintWriter out = response.getWriter();
        out.print(res);
        out.flush();
    }
}
