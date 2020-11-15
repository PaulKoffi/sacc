package sacc.proximityMessage;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import sacc.models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "sendProximityMsg",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/taskqueue/sendProximityMsg"
)
public class ProximityQueue extends HttpServlet {

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


        Queue queue = QueueFactory.getQueue("proximity-queue");
        queue.add(TaskOptions.Builder.withUrl("/sendProximityMsg").payload(payload));


        PrintWriter out = response.getWriter();
        out.print("Done");
        out.flush();
    }



}
