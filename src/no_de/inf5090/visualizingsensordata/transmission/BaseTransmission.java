package no_de.inf5090.visualizingsensordata.transmission;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class BaseTransmission implements Runnable {
	protected String mHostname;

	DefaultHttpClient httpClient = new DefaultHttpClient();

    /**
     * Constructor - make sure we get the correct remote host
     */
    BaseTransmission() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(VideoCapture.getSelf());
        mHostname = sharedPref.getString(VideoCapture.KEY_PREF_REMOTE_HOST, "");
    }

    /**
     * Check if transfer is enabled
     */
    public static boolean isTransferAvailable() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(VideoCapture.getSelf());
        return sharedPref.getBoolean(VideoCapture.KEY_PREF_REMOTE_ENABLED, false);
    }
	
	/**
	 * Transmission handling
	 */
	@Override
	public abstract void run();
}