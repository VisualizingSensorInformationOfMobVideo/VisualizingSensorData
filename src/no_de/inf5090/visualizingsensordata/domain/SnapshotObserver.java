package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SnapshotObserver extends AbstractDomainObservable implements Observer {
    private String encodedImage;
    private Observable mSnapshotSensor;

    /**
     * A the name for this sensor
     */
    public final static String NAME = "Snapshot";

    public SnapshotObserver(Observable snapshotSensor) {
    	mSnapshotSensor = snapshotSensor;
    }
    
    @Override
    public void update(Observable observable, Object data) {
        encodedImage = (String) data;
        
        setChanged();
        notifyObservers(new DomainData(this));
    }

    @Override
    public void onPause() {
        // controlled directly from controller when recording stops
    	mSnapshotSensor.deleteObserver(this);
    }

    @Override
    public void onResume() {
        // controlled directly from controller when recording start
        mSnapshotSensor.addObserver(this);
    }

    /**
     * Get the object itself (used by subclass)
     */
    protected SnapshotObserver getObj() {
        return this;
    }

    @Override
	public String getName() {
		return NAME;
	}
    
    /**
     * Sensor observer data
     */
    public class DomainData extends AbstractDomainData {
    	private String encodedImage;
    	
        protected DomainData(SnapshotObserver sensor) {
            super(sensor);
            this.encodedImage = sensor.encodedImage;
        }

        @Override
        public Element getXml(Document doc) {
            Element item = getBaseXml(doc);

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
