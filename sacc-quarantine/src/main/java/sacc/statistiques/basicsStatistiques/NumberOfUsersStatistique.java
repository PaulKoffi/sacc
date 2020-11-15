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
import java.util.Objects;
import java.util.concurrent.ExecutionException;


@WebServlet(name = "NumberOfUsersStatistique", value = "/statistiques/numberOfUsers")
public class NumberOfUsersStatistique extends HttpServlet {

  private Gson _gson = new Gson();
  private Firestore firestoreDb;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    connectToDatabase();

    DocumentReference docRef = firestoreDb.collection("statistics").document("sacc");
// asynchronously retrieve the document
    ApiFuture<DocumentSnapshot> future = docRef.get();
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
      System.out.println("Document data: " + document.getData());
    } else {
      System.out.println("No such document!");
    }
    Statistique statistique = new Statistique("number of users", Math.toIntExact((Long) Objects.requireNonNull(document.getData()).get("numberOfUsers")));
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