package no_de.inf5090.visualizingsensordata.domain;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Listen to the ROTATION_VECTOR-sensor for providing orientation data
 */
public class RotationVectorObserver extends LogicalSensorObservable implements SensorEventListener {
    private static final String TAG = "RotationVectorObserver";

    SensorManager sensorManager;
    protected float[] mSensorValues1; // for rotationvector-sensor or accelerometer in fallback
    protected float[] mSensorValues2; // for magneticfield-sensor in fallback

    protected Sensor mSensor1;
    protected Sensor mSensor2;

    private float mAzimuth; // rotation around the Z axis
    private float mPitch;   // rotation around the X axis
    private float mRoll;    // rotation around the Y axis

    private final float[] mRotationMatrix = new float[16];
    private final static float FILTER_SMOOTHING = 0.7f;

    private boolean mIsFallback = false;

    /**
     * The name for this sensor observer
     */
    public final static String NAME = "Rotation";

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
        // check if we don't have ROTATION_VECTOR-sensor available
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            Log.d(TAG, "Fallback to accelerometer and magnetic field sensors");
            mIsFallback = true;
        }

        this.sensorManager = sensorManager;
    }

    /**
     * Handle update from observable
     */
    public void onSensorChanged(SensorEvent event) {
        // make sure the sensor don't flood; time since last update should be above threshold
        long now = System.currentTimeMillis();
        long elapsed = now - mLastTime;
        if (elapsed < MINIMUM_DELAY) {
            Log.v(TAG, "data coming too fast from sensor, skipping");
            return;
        }
        mLastTime = now;

        // store the values for later use
        if (event.sensor == mSensor1)
            mSensorValues1 = event.values;
        else
            mSensorValues2 = event.values;

        // fallback?
        if (mIsFallback) {
            // require data from both sensors
            if (mSensorValues1 == null || mSensorValues2 == null) return;

            // get rotation matrix from sensors
            if (!SensorManager.getRotationMatrix(mRotationMatrix, null, mSensorValues1, mSensorValues2)) {
                Log.e(TAG, "getRotationMatrix failed");
                return;
            }
        } else {
            // transform rotation vector to rotation matrix
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, mSensorValues1);
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
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        if (mIsFallback) {
            if (mSensor1 == null) {
                mSensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }

            sensorManager.registerListener(this, mSensor1, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, mSensor2, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            if (mSensor1 == null) {
                mSensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            }

            sensorManager.registerListener(this,  mSensor1, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
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
	public String getName() {
		return NAME;
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
        public Element getXml(Document doc) {
            Element item = getBaseXml(doc), elm;

            // actual sensor data
            elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "azimuth");
            elm.appendChild(doc.createTextNode(Double.toString(getAzimuth())));
            item.appendChild(elm);

            elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "pitch");
            elm.appendChild(doc.createTextNode(Double.toString(getPitch())));
            item.appendChild(elm);

            elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "roll");
            elm.appendChild(doc.createTextNode(Double.toString(getRoll())));
            item.appendChild(elm);

            return item;
        }

        @Override
        public String getSensorName() {
            return getName();
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
