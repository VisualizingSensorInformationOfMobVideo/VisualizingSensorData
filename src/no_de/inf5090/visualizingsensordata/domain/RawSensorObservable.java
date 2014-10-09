package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * This type handles input from a sensor. Once values are changed, any registered listeners (Observers) will be notified
 */
public class RawSensorObservable extends Observable implements SensorEventListener {

	public float[] values;
	
	@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

    @Override
	public void onSensorChanged(SensorEvent event) {
		values = event.values;
		
		// Raise changed event
		setChanged();		// Sets changed to true
		notifyObservers();	// Notifies observers about a change
	}
	
}
