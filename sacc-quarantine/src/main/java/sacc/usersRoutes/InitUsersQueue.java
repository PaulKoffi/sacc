package sacc.usersRoutes;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import sacc.Location;
import sacc.models.Admin;
import sacc.models.Proximity;
import sacc.models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "InitUsers",
        description = "taskqueue: Enqueue a two positions with a key",
        urlPatterns = "/taskqueue/initUsers"
)

public class InitUsersQueue extends HttpServlet {
    private final Gson _gson = new Gson();
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Queue queue = QueueFactory.getDefaultQueue();
        for (int index =100; index < 300; index++){
            String res = _gson.toJson(new User("077779"+index,"florian.ainadou"+index+"@gmail.com", false, new Location(40.1f+index, 40.2f+index)));
            System.out.println(res);
            queue.add(TaskOptions.Builder.withUrl("/user").payload(res));

        }

        String res2 = _gson.toJson(new Admin("Paul", "koffixxxx@gmail.com"));
        queue.add(TaskOptions.Builder.withUrl("/admin").payload(res2));

        String res4 = _gson.toJson(new Admin("Paulmarie Djekinnou", "paulmarie.djekinnou@gmail.com"));
        queue.add(TaskOptions.Builder.withUrl("/admin").payload(res4));

        for (int index =100; index < 200; index++){

            String res3= _gson.toJson(new Proximity("077779"+index, "077779"+(index+1), new Location(45.1f+index, 47.2f+index),  new Location(45.1f+index, 47.2f+index)));
            queue.add(TaskOptions.Builder.withUrl("/user").payload(res3));

        }


        response.getWriter().print("Done");

    }
}
