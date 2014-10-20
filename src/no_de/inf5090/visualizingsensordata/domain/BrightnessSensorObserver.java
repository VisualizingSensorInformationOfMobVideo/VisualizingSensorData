package no_de.inf5090.visualizingsensordata.domain;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Measures brightness in the lux unit. It is not very accurate, and can
 * increase / decrease in large intervals, e.g. 10, 100, 1000, 10000
 *
 * At least on some phones, it will measure from the front side of the
 * phone (where the screen is)
 */
public class BrightnessSensorObserver extends LogicalSensorObservable implements SensorEventListener {
    private final SensorManager mSensorManager;
    private float mLux = 0.0f;

    /**
     * The name for this sensor observer
     */
    public final static String NAME = "Brightness";

    /**
     * Time of last update
     */
    private long mLastTime = System.currentTimeMillis();

    /**
     * Minimum delay between updates in milliseconds
     */
    private static final long MINIMUM_DELAY = 150;

    public BrightnessSensorObserver (SensorManager sensorManager){
        mSensorManager = sensorManager;
    }

    @Override
    //public void update(Observable observable, Object data) {
    public void onSensorChanged(SensorEvent event) {
        // make sure the sensor don't flood; time since last update should be above threshold
        long now = System.currentTimeMillis();
        long elapsed = now - mLastTime;
        mLastTime = now;
        if (elapsed < MINIMUM_DELAY) return;

        mLux = event.values[0];

        setChanged();
        notifyObservers(new LogicalSensorData(this));
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
	public String getName() {
		return NAME;
	}

    /** Return lux measured by this sensor */
    public float getLux() {
        return mLux;
    }

    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
    	private float mLux;
    	
        protected LogicalSensorData(BrightnessSensorObserver sensor) {
            super(sensor);
            mLux = sensor.getLux();
        }

        @Override
        public Element getXml(Document doc) {
            Element item = getBaseXml(doc);

            // actual sensor data
            Element elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "lux");
            elm.appendChild(doc.createTextNode(Float.toString(getLux())));
            item.appendChild(elm);

            return item;
        }

        @Override
        public String getSensorName() {
            return getName();
        }
        
        /**
         * Returns number of lux measured at the time this object was created
         * 
         * This method is needed because otherwise getXml() will call getLux() from
         * the outer class (BrightnessSensorObserver), and that value may have been
         * updated in the time between creation of this object and the call on getXml()
         */
        public float getLux() {
        	return mLux;
        }
    }

}
