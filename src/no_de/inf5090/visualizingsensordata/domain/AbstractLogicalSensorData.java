package no_de.inf5090.visualizingsensordata.domain;

import no_de.inf5090.visualizingsensordata.application.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * Generalized object to store and use data from sensors
 *
 * Actually retrieving data values from the sensor data
 * has to be customized for each different sensor, but this
 * class should provide a standard generation for XML-output
 * that will not be implementation-dependant
 */
public abstract class AbstractLogicalSensorData {
	/** Name of the XML element that contains data */
	public final static String xmlDataEntryName = "entry"; 
	
    /**
     * The sensor that generated this data
     */
    protected LogicalSensorObservable sensor;

    /**
     * Timestamp for sensor data
     */
    protected Date timestamp;

    /**
     * Constructor
     */
    protected AbstractLogicalSensorData(LogicalSensorObservable sensor) {
        this.sensor = sensor;
        this.timestamp = new Date();
    }

    /**
     * Get sensor
     */
    public LogicalSensorObservable getSensor() {
        return sensor;
    }

    /**
     * Get timestamp
     */
    public Date getTimestamp() {
		return timestamp;
	}

    /**
     * Generate XML-object for the sensor
     *
     * TODO: Document the implementation of this
     */
    public abstract Element getXml(Document doc);

    /**
     * All sensors should have a unique name to identify it
     */
    public abstract String getSensorName();

    /**
     * Get basic XML-DOM-element to inject data
     */
    protected Element getBaseXml(Document doc) {
        Element item = doc.createElement("logItem");
        Element elm;

        // name of sensor
        elm = doc.createElement("name");
        elm.appendChild(doc.createTextNode(getSensorName()));
        item.appendChild(elm);

        // timestamp of data
        item.setAttribute("dateTime", Utils.getDateString(getTimestamp()));

        return item;
    }
}
