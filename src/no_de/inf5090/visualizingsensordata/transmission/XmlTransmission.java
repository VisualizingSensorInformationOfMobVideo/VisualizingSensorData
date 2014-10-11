package no_de.inf5090.visualizingsensordata.transmission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class XmlTransmission implements Runnable {
	public final static String TAG = XmlTransmission.class.getSimpleName();
	public final static String HOSTNAME = "http://134.155.92.184";

	private StringEntity mStringEntity = null;

	public XmlTransmission(File f) {
		Log.d(TAG, "try to read file " + f.getName() + " exists " + f.exists());
		// String filePath = mContext.getFilesDir().getAbsolutePath();

		try {
			String content = getFileContents(f);
			mStringEntity = new StringEntity(content, HTTP.UTF_8);
		} catch (IOException e) {
			Log.e(TAG, "Error reading file thus nothing gets executed " + e.getMessage());
			mStringEntity = null;
		} finally {
			f.delete();
		}
	}

	@Override
	public void run() {
		Log.d(TAG, "Job gets executed...trying to send xml");

		if (mStringEntity == null) {
			Log.e(TAG, "Job aborted cause no content was provided.");
			return;
		}

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(HOSTNAME);
		mStringEntity.setContentType("text/xml");
		httpPost.setEntity(mStringEntity);

		try {
			HttpResponse response = httpClient.execute(httpPost);
			Log.d(TAG, "Job executed successfully. Response: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			Log.e(TAG, "Exception occured while trying to POST xml file " + e.getMessage());
		}
	}

	private String getFileContents(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		StringBuffer fileContent = new StringBuffer();

		byte[] buffer = new byte[1024];
		while (fis.read(buffer) != -1) {
			fileContent.append(new String(buffer, 0, fis.read(buffer)));
		}

		fis.close();
		return fileContent.toString();
	}
}
