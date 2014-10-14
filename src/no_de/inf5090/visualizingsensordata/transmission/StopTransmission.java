package no_de.inf5090.visualizingsensordata.transmission;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public final class StopTransmission extends BaseTransmission {
	public final static String TAG = "Transmission";
	AsyncTask<Void, Void, Void> mBackgroundHandler;
	
	@Override
	public void run() {
		if (TextUtils.isEmpty(mHostname)) {
			Log.e(TAG, "StopTransmission was aborted as hostname was not set.");
			Log.e(TAG, "mHostname " + mHostname);
			return;
		}
		
		mBackgroundHandler = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
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
				return null;
			}
		}.execute();
	}
}
