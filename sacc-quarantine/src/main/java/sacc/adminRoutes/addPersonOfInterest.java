package sacc.adminRoutes;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import sacc.models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "addPersonOfInterest",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/taskqueue/addPersonOfInterest"
)
public class addPersonOfInterest extends HttpServlet {
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
        queue.add(TaskOptions.Builder.withUrl("/user/putUserOfInterest").payload(payload));

    }
}
