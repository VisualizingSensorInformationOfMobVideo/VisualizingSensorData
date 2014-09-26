package no_de.inf5090.hqvideocapture.domain;

import java.util.Calendar;
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
	private final SensorObservable observableAccelerometer;
    private float mShake;					// Acceleration (without gravity, after filter).
    private float mPreviousAcceleration;	// The previous acceleration value (with gravity).
    private float mCurrentAcceleration;		// Current acceleration (with gravity).
    private Date lastTime;					// Time of last shake update.
    
    /**
     * Constructor method that initializes values.
     */ 
    public AccelerationSensorObserver(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mShake = 0.00f;
        lastTime = new Date();
        mPreviousAcceleration = SensorManager.GRAVITY_EARTH;
        mCurrentAcceleration = SensorManager.GRAVITY_EARTH;
    	observableAccelerometer = new SensorObservable();
		observableAccelerometer.addObserver(this);
    }
    
    /**
     * This method is executed on every sensor event update, but only does work if there's been some time since the last update.
     */ 
	public void update(Observable observable, Object data) {
		float x,y,z;	// Sensor values.	
		float mDelta;	// The change in acceleration.
		
		Calendar lastUpdateTime = Calendar.getInstance();
		Calendar currentTime = Calendar.getInstance();
		lastUpdateTime.setTime(lastTime);
		currentTime.setTime(new Date());
		
		if ((currentTime.getTimeInMillis() - lastUpdateTime.getTimeInMillis()) > 800) {
			
			x = observableAccelerometer.values[0];
			y = observableAccelerometer.values[1];
			z = observableAccelerometer.values[2];
			
			mPreviousAcceleration = mCurrentAcceleration;
			mCurrentAcceleration = (float) Math.sqrt((x * x) + (y * y) + (z * z));	// Find "magnitude" vector of current acceleration.
			mDelta = mCurrentAcceleration - mPreviousAcceleration;					// Get difference in acceleration, removing gravity.
			mShake = ((mShake * 0.9f + mDelta)/10);									// Apply low-pass filter to further reduce noise.
			setChanged();															// Notify observers of changes.
			notifyObservers(new SensorData(this, getShake(), new Date()));
		}
	}
	
    /**
     * Registers the sensor listener when the app is resumed to continue getting sensor data.
     */ 
	public void onResume() {
		mSensorManager.registerListener(observableAccelerometer, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
    /**
     * Releases the sensor listener when the app is paused to save power.
     */ 
	public void onPause() {
		mSensorManager.unregisterListener(observableAccelerometer);

	}
	
    /**
     * Returns the shaking value (filtered acceleration delta).
     */ 
	public float getShake() {
		return mShake;
	}
}