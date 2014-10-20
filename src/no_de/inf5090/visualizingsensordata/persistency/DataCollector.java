package no_de.inf5090.visualizingsensordata.persistency;

import android.util.Log;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AbstractDomainData;
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
    List<AbstractDomainData> mSensorData = new ArrayList<AbstractDomainData>();

    /**
     * We need access to camera object to fetch things like resolution
     */
    private CameraHelper mCameraHelper;

    /**
     * State of listening - if sensor data should be added to list or dropped
     */
    private boolean mIsRecording = false;

    /**
     * Unique id for request chain
     */
	private String mUniqueId;

    /**
     * Notification of a change in one of the sensors
     * Changes are collected
     */
    public void update(Observable observable, Object data) {
        synchronized(this) {
            if (!(data instanceof AbstractDomainData) || !mIsRecording) return;
            mSensorData.add((AbstractDomainData) data);
        }
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
    
    private String getUniqueId() {
    	if (mUniqueId == null)
    		mUniqueId = UUID.randomUUID().toString();
    	return mUniqueId;
    }

    /**
     * Generate XML-DOM for the data
     */
    protected Document generateXmlDom() {
        Element elm;
        Document doc = Utils.getXmlDocumentInstance();

        Element rootElement = doc.createElement("logFile");
        doc.appendChild(rootElement);

        // app name
        elm = doc.createElement("appName");
        elm.appendChild(doc.createTextNode("VisualizingSensorData"));
        rootElement.appendChild(elm);

        // id
        rootElement.setAttribute("id", getUniqueId());

        // datetime
        rootElement.setAttribute("dateTime", Utils.getDateString(new Date()));

        // phone model
        rootElement.setAttribute("phoneModel", android.os.Build.MODEL);

        // camera details
        elm = doc.createElement("camera");
        Element subelmResolution = doc.createElement("resolution");
        subelmResolution.appendChild(doc.createTextNode(mCameraHelper.getVideoResolution()));
        elm.appendChild(subelmResolution);

        Element subelmVerticalViewAngle = doc.createElement("verticalViewAngle");
        subelmVerticalViewAngle.appendChild(doc.createTextNode(Float.toString(mCameraHelper.getBaseVerticalViewAngle())));
        subelmVerticalViewAngle.setAttribute("type", mCameraHelper.getUnitOfViewAngle());
        elm.appendChild(subelmVerticalViewAngle);

        Element subelmHorizontalViewAngle = doc.createElement("horizontalViewAngle");
        subelmHorizontalViewAngle.appendChild(doc.createTextNode(Float.toString(mCameraHelper.getBaseHorizontalViewAngle())));
        subelmHorizontalViewAngle.setAttribute("type", mCameraHelper.getUnitOfViewAngle());
        elm.appendChild(subelmHorizontalViewAngle);

        rootElement.appendChild(elm); 

        // sensor data
        for (AbstractDomainData logItem : mSensorData) {
        	rootElement.appendChild(logItem.getXml(doc));
        }

        return doc;
    }

    /**
     * Fill a Writer-object with transformed XML data and empty the data list
     */
    protected void transformXml(Writer writer) {
        Document doc;
        synchronized (this) {
            doc = generateXmlDom();
            emptyData();
        }

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
        mSensorData = new ArrayList<AbstractDomainData>();
    }

    /**
     * Inject camera helper
     */
    public void setCameraHelper(CameraHelper ch) {
        mCameraHelper = ch;
    }
}
