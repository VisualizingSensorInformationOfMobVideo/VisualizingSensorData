package no_de.inf5090.visualizingsensordata.domain;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * This class gets sensor readings from the accelerometer and calculates the change
 * in acceleration before applying a filter. Hopefully this will allow the application to
 * detect shaking without being too influenced by gravity and the normal movement of the
 * person holding the phone.
 */
public class AccelerationSensorObserver extends Observable implements Observer {
	private final SensorManager mSensorManager;
	private final SensorObservable mObservableAccelerometer;
    private float mShake = 0.0f;                                 // Acceleration (without gravity, after filter).
    private float mAcceleration = SensorManager.GRAVITY_EARTH;   // The previous acceleration value (with gravity).
    
    /**
     * Time of last shake update
     */
    private long lastTime = System.currentTimeMillis();
    
    /**
     * Minimum delay between updates in milliseconds
     */
    private static final long MINIMUM_DELAY = 150;
    
    /**
     * Smoothening-constant for the low-pass filter
     */
    private static final float FILTER_SMOOTHING = .9f;
    
    /**
     * Constructor method that initializes values.
     */ 
    public AccelerationSensorObserver(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mObservableAccelerometer = new SensorObservable();
		mObservableAccelerometer.addObserver(this);
    }
    
    /**
     * This method is executed on every sensor event update, but only does work if there's been some time since the last update.
     */ 
	public void update(Observable observable, Object data) {
		// make sure the sensor don't flood; time since last update should be above threshold
		long now = System.currentTimeMillis();
		long elapsed = now - lastTime;
		lastTime = now;
		if (elapsed < MINIMUM_DELAY) return;
		
		float x = mObservableAccelerometer.values[0];
		float y = mObservableAccelerometer.values[1];
		float z = mObservableAccelerometer.values[2];
		
		float newAcceleration = (float) Math.sqrt((x * x) + (y * y) + (z * z));   // Find "magnitude" vector of current acceleration.
		float delta = newAcceleration - mAcceleration;                             // Get difference in acceleration, removing gravity.
		mAcceleration = newAcceleration;
		
		// low-pass filter
		// TODO: is this filter really necessary when having time delay?
		mShake = mShake * (1-FILTER_SMOOTHING) + delta * FILTER_SMOOTHING;
		
		setChanged();
		notifyObservers(new SensorData(this, getShake(), new Date()));
	}
	
    /**
     * Registers the sensor listener when the app is resumed to continue getting sensor data.
     */ 
	public void onResume() {
		mSensorManager.registerListener(mObservableAccelerometer, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
    /**
     * Releases the sensor listener when the app is paused to save power.
     */ 
	public void onPause() {
		mSensorManager.unregisterListener(mObservableAccelerometer);

	}
	
    /**
     * Returns the shaking value (filtered acceleration delta).
     */ 
	public float getShake() {
		return mShake;
	}
}