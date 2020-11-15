package sacc.uploadStatsFiles;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

@WebServlet(name = "uploadStatsFile",
        urlPatterns = "/uploadStatsFile"
)
public class uploadStatsFile extends HttpServlet {

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        // must delete all the files within the bucket before we can delete the bucket
        Iterator<Blob> list = storage.list("bucket_quarantine",
                Storage.BlobListOption.prefix("")).iterateAll()
                .iterator();
        list.forEachRemaining(blob -> blob.delete());
        System.out.println("Delete all successfully !!!!");
    }

}
