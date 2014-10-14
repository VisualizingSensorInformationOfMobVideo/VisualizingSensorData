package no_de.inf5090.visualizingsensordata.transmission;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public final class XmlTransmission extends BaseTransmission {
	public final static String TAG = XmlTransmission.class.getSimpleName();

	private StringEntity mStringEntity = null;

	public XmlTransmission(String xml) {
        super();
		try {
			mStringEntity = new StringEntity(xml, HTTP.UTF_8);
		} catch (IOException e) {
			Log.e(TAG, "Error while converting string to StringEntitiy " + e.getMessage());
			mStringEntity = null;
		}
	}

	@Override
	public void run() {
		Log.d(TAG, "Job gets executed...trying to send xml");

		if (mStringEntity == null) {
			Log.e(TAG, "Job aborted cause no content was provided.");
			return;
		}

		HttpPost httpPost = new HttpPost(mHostname);
		mStringEntity.setContentType("text/xml");
		httpPost.setEntity(mStringEntity);

		try {
			HttpResponse response = httpClient.execute(httpPost);
			Log.d(TAG, "Job executed successfully. Response: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			Log.e(TAG, "Exception occured while trying to POST xml file " + e.getMessage());
		}
	}
}
