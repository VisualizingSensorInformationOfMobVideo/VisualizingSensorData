package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.application.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SnapshotObserver extends LogicalSensorObservable implements Observer {
    private String encodedImage;

    /**
     * A the name for this sensor
     */
    public final static String NAME = "Snapshot";

    /**
     * Time of last update
     */
    private long mLastTime = System.currentTimeMillis();

    /**
     * Minimum delay between updates in milliseconds
     */
    private static final long MINIMUM_DELAY = 150;

    @Override
    public void update(Observable observable, Object data) {
        // make sure the sensor don't flood; time since last update should be above threshold
        long now = System.currentTimeMillis();
        long elapsed = now - mLastTime;
        mLastTime = now;
        if (elapsed < MINIMUM_DELAY) return;

        /*
         * Do something with the data updated
         */
        encodedImage = (String) data;
        
        setChanged();
        notifyObservers(new LogicalSensorData(this));
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        /* Stop taking pictures here */
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        /* Continue taking pictures here */
    }

    @Override
	public String getName() {
		return NAME;
	}
    
    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
    	private String encodedImage;
    	
        protected LogicalSensorData(SnapshotObserver sensor) {
            super(sensor);
            this.encodedImage = sensor.encodedImage;
        }

        @Override
        public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml();

            // actual sensor data
            Element elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "base64data");
            elm.appendChild(doc.createTextNode(encodedImage));
            item.appendChild(elm);

            return item;
        }

        @Override
        public String getSensorName() {
            return getName();
        }

    }

}
