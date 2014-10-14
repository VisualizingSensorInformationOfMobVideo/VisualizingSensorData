package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import no_de.inf5090.visualizingsensordata.application.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class gets sensor readings from the accelerometer and calculates the change
 * in acceleration before applying a filter. Hopefully this will allow the application to
 * detect shaking without being too influenced by gravity and the normal movement of the
 * person holding the phone.
 */
public class AccelerationSensorObserver extends LogicalSensorObservable implements Observer {
    private final SensorManager mSensorManager;
    private final RawSensorObservable mObservableAccelerometer;
    private float mShake = 0.0f;                                 // Acceleration (without gravity, after filter).
    private float mAcceleration = SensorManager.GRAVITY_EARTH;   // The previous acceleration value (with gravity).

    /**
     * The name for this sensor observer
     */
    public final static String NAME = "Acceleration";
    
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
        mObservableAccelerometer = new RawSensorObservable();
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
        notifyObservers(new LogicalSensorData(this));
    }

    @Override
    public void onResume() {
        mSensorManager.registerListener(mObservableAccelerometer, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mObservableAccelerometer);
    }

    /**
     * Returns the shaking value (filtered acceleration delta).
     */
    public float getShake() {
        return mShake;
    }

    @Override
	public String getName() {
		return NAME;
	}
    
    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
        private float acceleration;

        public LogicalSensorData(AccelerationSensorObserver sensor) {
            super(sensor);
            this.acceleration = sensor.getShake();
        }

        @Override
        public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml();

            // actual sensor data
            Element elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "force");
            elm.appendChild(doc.createTextNode(Float.toString(getAcceleration())));
            item.appendChild(elm);

            return item;
        }

        /**
         * Returns the current value
         */
        public float getAcceleration() {
            return acceleration;
        }

        @Override
        public String getSensorName() {
            return getName();
        }
    }
}