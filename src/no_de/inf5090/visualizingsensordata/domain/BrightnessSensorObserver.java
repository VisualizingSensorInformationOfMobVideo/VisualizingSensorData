package no_de.inf5090.visualizingsensordata.domain;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.SnapshotObserver.LogicalSensorData;

/**
 * Measures brightness in the lux unit. It is not very accurate, and can 
 * increase / decrease in large intervals, e.g. 10, 100, 1000, 10000 
 *
 * At least on some phones, it will measure from the front side of the 
 * phone (where the screen is) 
 */
public class BrightnessSensorObserver extends LogicalSensorObservable implements Observer {
	private final SensorManager mSensorManager;
	private final RawSensorObservable mObservableBrightness;
	private float mLux = 0.0f;

    /**
     * The unique ID for this sensor observer
     */
    public final static int ID = 105;
    
    /**
     * The unique ID for this sensor observer
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
        mObservableBrightness = new RawSensorObservable();
		mObservableBrightness.addObserver(this);
    }
    
	@Override
	public void update(Observable observable, Object data) {
		// make sure the sensor don't flood; time since last update should be above threshold
		long now = System.currentTimeMillis();
		long elapsed = now - mLastTime;
		mLastTime = now;
		if (elapsed < MINIMUM_DELAY) return;
		
		mLux = mObservableBrightness.values[0];
		
		setChanged();
		notifyObservers(new LogicalSensorData(this));
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(mObservableBrightness);
	}

	@Override
	public void onResume() {
		mSensorManager.registerListener(mObservableBrightness, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public int getSensorID() {
		return ID;
	}
	
	/** Return lux measured by this sensor */
	public float getLux() {
		return mLux;
	}
	
	/**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {

		protected LogicalSensorData(LogicalSensorObservable sensor) {
			super(sensor);
		}

		@Override
		public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml();

            // actual sensor data
            Element elm = doc.createElement("Entry");
            elm.setAttribute("type", "lux");
            elm.appendChild(doc.createTextNode(Float.toString(getLux())));
            item.appendChild(elm);

            return item;
		}

		@Override
		public int getSensorID() {
			return ID;
		}

		@Override
		public String getSensorName() {
			return NAME;
		}
    }

}
