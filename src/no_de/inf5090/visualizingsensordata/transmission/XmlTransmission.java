package no_de.inf5090.visualizingsensordata.transmission;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

/**
 * Class for sending XML
 */
public class XmlTransmission {

        public void dataSend(String fpath, File file) {
            //DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("134.155.92.184");
            String filePath = this.getFilesDir().getAbsolutePath();
            File f=new File(filePath,"file.xml");
            //byte[] data = FileOperator.readBytesFromFile(f);
            String content=getFileContent(file);
            StringEntity se = null;
            try {
                se = new StringEntity(content, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            se.setContentType("text/xml");
            httppost.setEntity(se);
            f.delete();
        }

        private File getFilesDir() {
            // TODO Auto-generated method stub
            return null;
        }

        private String getFileContent(File file) {
            // TODO Auto-generated method stub
            return null;
        }
}