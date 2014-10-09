package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.application.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SnapshotObserver extends LogicalSensorObservable implements Observer {

    /**
     * The unique ID for this sensor observer
     */
    public final static int ID = 101;
    
    /**
     * The unique ID for this sensor observer
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
	public int getSensorID() {
		return ID;
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
            elm.setAttribute("type", "base64data");
            elm.appendChild(doc.createTextNode("base64data lots of data"));
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
