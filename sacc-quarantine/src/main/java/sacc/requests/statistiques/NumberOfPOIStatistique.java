package sacc.requests.statistiques;

import com.google.appengine.repackaged.com.google.gson.Gson;
import sacc.mocks.Statistiques;
import sacc.models.Statistique;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "NumberOfPOIStatistique", value = "/statistiques/numberOfPOI")
public class NumberOfPOIStatistique extends HttpServlet {

    private Gson _gson = new Gson();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Statistiques statistiques = new Statistiques();

        sendAsJson(response,statistiques.getnumberOfPOIStatistique());
    }

    private void sendAsJson( HttpServletResponse response, Object obj) throws IOException {
        response.setContentType("application/json");
        String res = _gson.toJson(obj);
        PrintWriter out = response.getWriter();
        out.print(res);
        out.flush();
    }

}
