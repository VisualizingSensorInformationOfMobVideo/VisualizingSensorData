package no_de.inf5090.hqvideocapture.domain;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * This class keeps track of the rotation of the device
 * @author aage
 *
 */
public class RotationSensorObserver extends Observable implements Observer {

	SensorManager sensorManager;
	SensorObservable observableAccelerometer;
	SensorObservable observableMagneticField;
	
	private float angY;
	
	public enum Orientation {
		LANDSCAPE, PORTRAIT;
	}

	// Orientation of device - influences zero point of rotation
	private Orientation orientation = Orientation.LANDSCAPE;
	
	public float inclination;
	
	/*
	 * @param sensorManager: Instance of SensorManager. Needed to get access to sensors. 
	 * Note: need to call acquire resources to start listening for sensor changes!
	 */
	public RotationSensorObserver(SensorManager sensorManager) {
		// TODO Auto-generated constructor stub
		observableAccelerometer = new SensorObservable();
		observableAccelerometer.addObserver(this);

		observableMagneticField = new SensorObservable();
		observableMagneticField.addObserver(this);
		
		// Get instance of SensorManager
		this.sensorManager = sensorManager;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object data) {
		// One of the sensors where updated
		float[] R = new float[9];	// Rotation
		float[] I = new float[9];	// Inclination
		float[] values = new float[3];
		
		// No sensor values
		if(observableAccelerometer.values == null || observableMagneticField.values == null)
			return;
		
		// Unable to calc rotation matrix - always fails in debug
		if(!SensorManager.getRotationMatrix(R, I, observableAccelerometer.values, observableMagneticField.values)) {
			/*
			angY = -0.5f;
			inclination = 1f;
			setChanged();
			notifyObservers();
			*/
			return;
		}
		
		// Calculate orientation of phone
		float[] orientation = SensorManager.getOrientation(R, values);
		angY = orientation[2];		

		// Calculate inclination of phone
		inclination = SensorManager.getInclination(I);
		
		// Inform observers:
		setChanged();
		try {
			notifyObservers(new SensorData(this, (float)(getRoll() / Math.PI), new Date()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Call this function to release resources (when going to pause mode in activity)
	 * Note: must call acquireResources() to enable survailing of orientation 
	 */
	public void freeResources() {
		
		// Release resources (sensors) to save battery
		sensorManager.unregisterListener(observableAccelerometer);
		sensorManager.unregisterListener(observableMagneticField);
	}
	
	/*
	 * Call this to start listening for changes in orientation
	 */
	public void acquireResources() {
		
		// Register sensors to listen to
		sensorManager.registerListener(observableAccelerometer, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(observableMagneticField, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/*
	 * This updates the sensor to reorient the device
	 */
	public void setOrientation(Orientation orientation) {
		if(this.orientation != orientation) {
			this.orientation = orientation;
			
			// Inform observers of change
			setChanged();
			notifyObservers();			
		}
	}
	
	/*
	 * Gets the roll angle in radians of the device
	 */
	public double getRoll() {
		return angY + (orientation == Orientation.LANDSCAPE ? Math.PI/2 : 0);
	}
	
	/*
	 * Normalizes absolute values of angles between -pi and pi
	 * @param angle: Angle in radians to normalize
	 * @return: Normalized value between 0 and 1 (0 == 0, +-pi == 1)
	 * @throw: Throws exception if angle is outside of the range +-pi
	 */
	static public double normalizeRadianAngle(double angle) throws Exception {
		if((angle < -Math.PI) || (angle > Math.PI))
			throw new Exception("Angle cannot be greater than pi");
		return Math.abs(angle / (Math.PI));
	}

}
