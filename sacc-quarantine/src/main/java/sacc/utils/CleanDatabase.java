package sacc.utils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "clean",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/taskqueue/clean"
)
public class CleanDatabase extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/user").method(TaskOptions.Method.DELETE));
        queue.add(TaskOptions.Builder.withUrl("/admin").method(TaskOptions.Method.DELETE));
        queue.add(TaskOptions.Builder.withUrl("/sendProximityMsg").method(TaskOptions.Method.DELETE));
        queue.add(TaskOptions.Builder.withUrl("/uploadStatsFile").method(TaskOptions.Method.DELETE));

    }
}
