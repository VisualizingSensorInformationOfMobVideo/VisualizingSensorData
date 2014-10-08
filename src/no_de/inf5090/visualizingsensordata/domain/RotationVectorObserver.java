package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import no_de.inf5090.visualizingsensordata.application.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Listen to the ROTATION_VECTOR-sensor for providing orientation data
 */
public class RotationVectorObserver extends LogicalSensorObservable implements Observer {
	private static final String TAG = "RotationVectorObserver";
	
	SensorManager sensorManager;
	RawSensorObservable mObservableSensor1; // for rotationvector-sensor or accelerometer in fallback
	RawSensorObservable mObservableSensor2; // for magneticfield-sensor in fallback
	
	private float mAzimuth; // rotation around the Z axis
	private float mPitch;   // rotation around the X axis
	private float mRoll;    // rotation around the Y axis
	
	private final float[] mRotationMatrix = new float[16];
	private final static float FILTER_SMOOTHING = 0.7f;
	
	private boolean mIsFallback = false;

    /**
     * The unique ID for this sensor observer
     */
    public final static int ID = 102;

    /**
     * Time of last update
     */
    private long mLastTime = System.currentTimeMillis();
    
    /**
     * Minimum delay between updates in milliseconds
     */
    private static final long MINIMUM_DELAY = 150;
    
    /**
	 * @param sensorManager: Instance of SensorManager. Needed to get access to sensors. 
	 * Note: need to call acquire resources to start listening for sensor changes!
	 */
	public RotationVectorObserver(SensorManager sensorManager) {
		mObservableSensor1 = new RawSensorObservable();
		mObservableSensor1.addObserver(this);
		
		// check if we don't have ROTATION_VECTOR-sensor available
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
			Log.d(TAG, "Fallback to accelerometer and magnetic field sensors");
			mObservableSensor2 = new RawSensorObservable();
			mObservableSensor2.addObserver(this);
			mIsFallback = true;
		}

		this.sensorManager = sensorManager;
	}
	
	/**
	 * Handle update from observable
	 */
	public void update(Observable observable, Object data) {
		// make sure the sensor don't flood; time since last update should be above threshold
		long now = System.currentTimeMillis();
		long elapsed = now - mLastTime;
		if (elapsed < MINIMUM_DELAY) {
			Log.v(TAG, "data coming too fast from sensor, skipping");
			return;
		}
		mLastTime = now;
		
		// fallback?
		if (mIsFallback) {
			// require data from both sensors
			if (mObservableSensor1.values == null || mObservableSensor2.values == null) return;
			
			// get rotation matrix from sensors
			if (!SensorManager.getRotationMatrix(mRotationMatrix, null, mObservableSensor1.values, mObservableSensor2.values)) {
				Log.e(TAG, "getRotationMatrix failed");
				return;
			}
		} else {
			// transform rotation vector to rotation matrix
			SensorManager.getRotationMatrixFromVector(mRotationMatrix, mObservableSensor1.values);
		}
		
		// transform coordinate system
		SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
		
		// transform rotation matrix to orientation
		float values[] = new float[3];
		SensorManager.getOrientation(mRotationMatrix, values);
		
		// make roll be 0 on normal landscape
		values[2] += Math.PI/2;
		if (values[2] > Math.PI) values[2] -= 2*Math.PI;
		
        // apply low-pass filter
		mAzimuth = values[0] * FILTER_SMOOTHING + mAzimuth * (1-FILTER_SMOOTHING);
		mPitch   = values[1] * FILTER_SMOOTHING + mPitch   * (1-FILTER_SMOOTHING);
		mRoll    = values[2] * FILTER_SMOOTHING + mRoll    * (1-FILTER_SMOOTHING);
		
		// inform observers
		setChanged();
		notifyObservers(new LogicalSensorData(this));
	}

    @Override
    public void onPause() {
        sensorManager.unregisterListener(mObservableSensor1);
        if (mIsFallback) sensorManager.unregisterListener(mObservableSensor2);
    }

    @Override
    public void onResume() {
        if (mIsFallback) {
            sensorManager.registerListener(mObservableSensor1, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(mObservableSensor2, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            sensorManager.registerListener(mObservableSensor1,  sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
	
	/**
	 * Gets azimuth (in radians, [-PI,PI])
	 */
	public double getAzimuth() {
		return mAzimuth;
	}
	
	/**
	 * Gets pitch (in radians, [-PI/2,PI/2])
	 */
	public double getPitch() {
		return mPitch;
	}
	
	/**
	 * Gets roll (in radians, [-PI,PI], 0 is landscape like the view itself)
	 */
	public double getRoll() {
		return mRoll;
	}

    @Override
    public int getSensorID() {
        return ID;
    }

    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
        private double mAzimuth;
        private double mPitch;
        private double mRoll;

        public LogicalSensorData(RotationVectorObserver sensor) {
            super(sensor);
            this.mAzimuth = sensor.getAzimuth();
            this.mPitch = sensor.getPitch();
            this.mRoll = sensor.getRoll();
        }

        @Override
        public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml(), elm;

            // actual sensor data
            elm = doc.createElement("Entry");
            elm.setAttribute("type", "azimuth");
            elm.appendChild(doc.createTextNode(Double.toString(getAzimuth())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "pitch");
            elm.appendChild(doc.createTextNode(Double.toString(getPitch())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "roll");
            elm.appendChild(doc.createTextNode(Double.toString(getRoll())));
            item.appendChild(elm);

            return item;
        }

        @Override
        public int getSensorID() {
            return ID;
        }

        @Override
        public String getSensorName() {
            return "Rotation";
        }

        /**
         * Gets azimuth (in radians, [-PI,PI])
         */
        public double getAzimuth() {
            return mAzimuth;
        }

        /**
         * Gets pitch (in radians, [-PI/2,PI/2])
         */
        public double getPitch() {
            return mPitch;
        }

        /**
         * Gets roll (in radians, [-PI,PI], 0 is landscape like the view itself)
         */
        public double getRoll() {
            return mRoll;
        }
    }
}
