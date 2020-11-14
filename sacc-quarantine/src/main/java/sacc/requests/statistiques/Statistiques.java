package sacc.requests.statistiques;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import sacc.models.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

@WebServlet(name = "Statistiques", value = "/statistiques")
public class Statistiques extends HttpServlet {

  private Gson _gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    User user = new User("0219410941","ariohfa",true);

    sendAsJson(response,user);
  }

  private void sendAsJson(
          HttpServletResponse response,
          Object obj) throws IOException {
    response.setContentType("application/json");
    System.out.printf(obj.toString());
    String res = _gson.toJson(obj);
    PrintWriter out = response.getWriter();
    out.print(res);
    out.flush();
  }

}