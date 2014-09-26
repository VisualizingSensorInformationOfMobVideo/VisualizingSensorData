package no_de.inf5090.hqvideocapture.domain;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/*
 * This type handles input from a sensor. Once values are changed, any registered listeners (Observers) will be notified
 */
public class SensorObservable extends Observable implements SensorEventListener {

	public float[] values;
	
	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	public void onSensorChanged(SensorEvent event) {
		values = event.values;
		
		// Default error 
		if(values.length < 3) {
			values = new float[]{-999f,-999f,-999f};
		}

		// Raise changed event
		setChanged();		// Sets changed to true
		notifyObservers();	// Notifies observers about a change
	}
	
}
