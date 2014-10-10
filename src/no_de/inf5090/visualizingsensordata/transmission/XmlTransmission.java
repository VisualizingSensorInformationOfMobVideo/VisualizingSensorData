package no_de.inf5090.visualizingsensordata.transmission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;



import no_de.inf5090.visualizingsensordata.persistency.DataCollector;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.content.Context;

public class XmlTransmission extends DataCollector {
    private Context mContext;

    private final static String HOSTNAME = "134.155.92.184";
    private final static String FILENAME = "file.xml";

    public XmlTransmission(Context context) {
        mContext = context;
    }

    public void dataSend(String fpath, File file) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HOSTNAME);

        String filePath = mContext.getFilesDir().getAbsolutePath();
        File f = new File(filePath, FILENAME);

        String content = getFileContents(file);
        StringEntity se = null;
        try {
            se = new StringEntity(content, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        se.setContentType("text/xml");
        httpPost.setEntity(se);
        try {
			httpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        f.delete();
    }

    /**
     * @throws IOException 
     * @see http://stackoverflow.com/a/9095689
     */
    private String getFileContents(File file) throws IOException {
        FileInputStream fis = null;
        fis = mContext.openFileInput("test.txt");
        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        while (fis.read(buffer) != -1)
        {
            fileContent.append(new String(buffer, 0, fis.read(buffer)));
        }
        return fileContent.toString();
    }
}
