package sacc.statistiques.basicsStatistiques;

import com.google.api.core.ApiFuture;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import sacc.models.Statistique;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


@WebServlet(name = "NumberOfUsersStatistique", value = "/statistiques/numberOfUsers")
public class NumberOfUsersStatistique extends HttpServlet {

  private Gson _gson = new Gson();
  private Firestore firestoreDb;
  private int numberOfUsers = 0;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    connectToDatabase();

    //connectToDatabase();
    Iterable<DocumentReference> docRef = firestoreDb.collection("users").listDocuments();
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
        for (Map.Entry<String, Object> entry : map.entrySet()) {
          numberOfUsers++;
        }
      } else {
        System.out.println("No such document!");
      }
    });
    Statistique statistique = new Statistique("number of users", numberOfUsers);
    sendAsJson(response, Objects.requireNonNull(statistique));
  }

  private void sendAsJson( HttpServletResponse response, Object obj) throws IOException {
    response.setContentType("application/json");
    String res = _gson.toJson(obj);
    PrintWriter out = response.getWriter();
    out.print(res);
    out.flush();
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