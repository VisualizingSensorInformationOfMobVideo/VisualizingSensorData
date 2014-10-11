package no_de.inf5090.visualizingsensordata.transmission;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransmissionService {
	private ScheduledExecutorService executor;

	public static TransmissionService instance;

	public static TransmissionService getService() {
		if (instance == null)
			instance = new TransmissionService();
		return instance;
	}

	private TransmissionService() {
		executor = Executors.newSingleThreadScheduledExecutor();
		// executor = Executors.newScheduledThreadPool(2);
	}

	public void addTransmission(Runnable job) {
		executor.schedule(job, 300, TimeUnit.MILLISECONDS);
	}

}
