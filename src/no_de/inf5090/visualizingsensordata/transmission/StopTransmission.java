package no_de.inf5090.visualizingsensordata.transmission;

import java.io.IOException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public final class StopTransmission extends BaseTransmission {
	public final static String TAG = StopTransmission.class.getSimpleName();
	
	@Override
	public void run() {
		StringEntity mStringEntity;
		
		try {
			mStringEntity = new StringEntity("stop", HTTP.UTF_8);
		} catch (IOException e) {
			Log.e(TAG, "Error while converting string to StringEntitiy " + e.getMessage());
			mStringEntity = null;
		}
		
		HttpPost httpPost = new HttpPost(mHostname);
		mStringEntity.setContentType("text/plain");
		httpPost.setEntity(mStringEntity);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			Log.d(TAG, "Job executed successfully. Response: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			Log.e(TAG, "Exception occured while trying to POST xml file " + e.getMessage());
		}
	}
}
