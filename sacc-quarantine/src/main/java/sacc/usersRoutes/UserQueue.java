package sacc.usersRoutes;

import com.google.appengine.api.utils.SystemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import sacc.utils.Sha1Hash;

@WebServlet(name = "userQueue",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/taskqueue/user"
        )
public class UserQueue extends HttpServlet {

  private Gson _gson = null;


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


    Queue queue = QueueFactory.getQueue("users-queue");
    queue.add(TaskOptions.Builder.withUrl("/users").payload(payload));
    response.getWriter().print("Done");

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
    PrintWriter out = response.getWriter();
    out.print(res);
    out.flush();
  }
}
