package no_de.inf5090.visualizingsensordata.persistency;

import android.util.Log;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AbstractLogicalSensorData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * The data collector receives sensor data and is used to extract data for specified time slices.
 *
 * E.g. for the remote connection, we want data between each remote push, while for
 * the local storage we might want to keep all data until we generate the XML-file.
 *
 * The class is extended by the objects that is used to persist/transfer data
 */
public abstract class DataCollector implements Observer {
    /**
     * List for sensor data from observed logical sensors
     */
    List<AbstractLogicalSensorData> mSensorData = new ArrayList<AbstractLogicalSensorData>();

    /**
     * State of listening - if sensor data should be added to list or dropped
     */
    private boolean mIsRecording = false;

    /**
     * Notification of a change in one of the sensors
     * Changes are collected
     */
    public void update(Observable observable, Object data) {
        if (!(data instanceof AbstractLogicalSensorData) || !mIsRecording) return;
        mSensorData.add((AbstractLogicalSensorData) data);
    }

    /**
     * Start recording for logical sensor updates. The sensors have to be injected in main controller.
     */
    public void startRecording() {
        mIsRecording = true;
    }

    /**
     * Stop recording for logical sensor updates
     */
    public void stopRecording() {
        mIsRecording = false;
    }

    /**
     * Generate XML-DOM for the data
     */
    protected Document generateXmlDom() {
        Element elm;
        Document doc = Utils.getDocumentInstance();
        Element rootElement = doc.getDocumentElement(); 
        
        // Check if this document already has been created
        if (rootElement == null) {
	        rootElement = doc.createElement("LogFile");
	        doc.appendChild(rootElement);
	
	        // app name
	        elm = doc.createElement("AppName");
	        elm.appendChild(doc.createTextNode("VisualizingSensorData"));
	        rootElement.appendChild(elm);
	
	        // datetime
	        rootElement.setAttribute("dateTime", Utils.getDateString(new Date()));
        }
        
        // sensor data
        for (AbstractLogicalSensorData logItem : mSensorData) {
            rootElement.appendChild(logItem.getXml());
        }

        return doc;
    }

    /**
     * Fill a Writer-object with transformed XML data and empty the data list
     */
    protected void transformXml(Writer writer) {
        Document doc = generateXmlDom();
        emptyData();

        DOMSource source = new DOMSource(doc);

        // Set up transformer
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            Properties outFormat = new Properties();
            outFormat.setProperty(OutputKeys.INDENT, "yes");
            outFormat.setProperty(OutputKeys.METHOD, "xml");
            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            outFormat.setProperty(OutputKeys.VERSION, "1.0");
            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperties(outFormat);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, new StreamResult(writer));
        //} catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
            Log.e("DataCollector", "TransformerException detected!");
        }
    }

    /**
     * Generate XML data as a string and empty the data list
     */
    protected String getXmlString() {
        StringWriter writer = new StringWriter();
        transformXml(writer);
        return writer.getBuffer().toString();
    }

    /**
     * Empty list of sensor data (e.g. after XML is generated)
     */
    protected void emptyData() {
        mSensorData = new ArrayList<AbstractLogicalSensorData>();
    }
}
