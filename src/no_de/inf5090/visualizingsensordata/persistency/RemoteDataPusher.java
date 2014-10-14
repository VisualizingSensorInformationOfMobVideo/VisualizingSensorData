package no_de.inf5090.visualizingsensordata.persistency;

import no_de.inf5090.visualizingsensordata.transmission.XmlTransmission;
import android.os.AsyncTask;
import android.util.Log;

public class RemoteDataPusher extends DataCollector {
	/**
	 * Interval between pushes
	 */
	public static final long DELAY = 1000 * 3;
	public static final String TAG = "Transmission";

	AsyncTask<Void, Void, Void> mBackgroundHandler;
	// we actually do not care...
	private boolean shouldRun = true;

	public RemoteDataPusher() {
		// background handler 
		mBackgroundHandler = new AsyncTask<Void, Void, Void>() {

			protected Void doInBackground(Void... params) {
				// loop
				while (shouldRun) {
					XmlTransmission trans = new XmlTransmission(getXmlString());
					trans.run();
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException e) {
						// should never occur
						Log.e(TAG, "Sleep failed");
					}
				}

				return null;
			};
		}.execute();
	}

	public void finish() {
		mBackgroundHandler.cancel(true);
		// just to be sure...
		shouldRun = false;
	}

}
