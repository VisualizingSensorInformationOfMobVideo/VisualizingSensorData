package no_de.inf5090.visualizingsensordata.transmission;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
/*
public class XmlTransmission {
    private Context mContext;

    private final static String HOSTNAME = "134.155.92.184";
    private final static String FILENAME = "file.xml";

    public XmlTransmission(Context context) {
        mContext = context;
    }

    public void dataSend(String fpath, File file) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HOSTNAME);

        String filePath = mContext.getFilesDir().getAbsolutePath();
        File f = new File(filePath, FILENAME);

        String content = getFileContent(file);
        StringEntity se = null;
        try {
            se = new StringEntity(content, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        se.setContentType("text/xml");
        httpPost.setEntity(se);
        httpClient.execute(httpPost);
        f.delete();
    }*/

    /**
     * @see http://stackoverflow.com/a/9095689
     *
    private String getFileContent(File file) {
        FileInputStream fis;
        fis = openFileInput("test.txt");
        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        while ((n = fis.read(buffer)) != -1)
        {
            fileContent.append(new String(buffer, 0, n));
        }
        return fileContent;
    }
}
*/