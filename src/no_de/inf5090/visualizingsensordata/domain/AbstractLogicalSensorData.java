package no_de.inf5090.visualizingsensordata.domain;

import no_de.inf5090.visualizingsensordata.application.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Generalized object to store and use data from sensors
 *
 * Actually retrieving data values from the sensor data
 * has to be customized for each different sensor, but this
 * class should provide a standard generation for XML-output
 * that will not be implementation-dependant
 */
public abstract class AbstractLogicalSensorData {
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
    public abstract Element getXml();

    /**
     * All sensors should have a unique ID to identify it
     */
    public abstract int getSensorID();

    /**
     * All sensors should have a unique name to identify it
     */
    public abstract String getSensorName();

    /**
     * Get basic XML-DOM-element to inject data
     */
    protected Element getBaseXml() {
        Document doc = Utils.getDocumentInstance();

        Element item = doc.createElement("LogItem");
        Element elm;

        // name of sensor
        elm = doc.createElement("Name");
        elm.appendChild(doc.createTextNode(getSensorName()));
        item.appendChild(elm);

        // timestamp of data
        item.setAttribute("dateTime", Utils.getDateString(getTimestamp()));

        return item;
    }
}
