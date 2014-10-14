package no_de.inf5090.visualizingsensordata.transmission;

import org.apache.http.impl.client.DefaultHttpClient;

public abstract class BaseTransmission implements Runnable {
	public final static String HOSTNAME = "http://134.155.92.184";

	DefaultHttpClient httpClient = new DefaultHttpClient();
	
	/**
	 * Transmission handling
	 */
	@Override
	public abstract void run();
}