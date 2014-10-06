package no_de.inf5090.visualizingsensordata.persistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AbstractLogicalSensorData;

/**
 * This class logs all changes in sensors. When writeXML is executed, the readings will be written to file in xml format
 * 
 * @author aage et al.
 *
 */
public class SensorWriter implements Observer {

	List<AbstractLogicalSensorData> sensorData;

    private boolean mIsRecording = false;
	
	/**
	 * Constructor: initiates sensorwriter
	 */
	public SensorWriter() {
		sensorData = new ArrayList<AbstractLogicalSensorData>();
	}
	
	/**
	 * Notification of a change in one of the sensors
	 * Changes are collected
	 */
	public void update(Observable observable, Object data) {
        if (!(data instanceof AbstractLogicalSensorData) || !mIsRecording) return;

        sensorData.add((AbstractLogicalSensorData) data);
        // FIXME: is this needed?? Utils.sensorDatas.add((SensorData) data);
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
     * Convert data into XML-objects
     */
    protected Document generateXmlDom() {
       	Element elm;
        Document doc = Utils.getDocumentInstance();
        Element rootElement = doc.createElement("LogFile");
        doc.appendChild(rootElement);

        // app name
        elm = doc.createElement("AppName");
        elm.appendChild(doc.createTextNode("VisualizingSensorData"));
        rootElement.appendChild(elm);

        // datetime
        elm = doc.createElement("DateTime");
        elm.appendChild(doc.createTextNode(Utils.getDateString(new Date())));
        rootElement.appendChild(elm);

        // sensor data
        for (AbstractLogicalSensorData logItem : sensorData) {
            rootElement.appendChild(logItem.getXml());
        }

        return doc;
	}

    /**
     * Empty data set
     */
    public void emptyData() {
        sensorData = new ArrayList<AbstractLogicalSensorData>();
    }

    /**
     * Generate XML-file and empty data set
     */
    public void writeXml(String fpath) {
        Document doc = generateXmlDom();
        emptyData();

        File file = new File (fpath);
        if (file.exists ()) file.delete();

        try {
            // Write to xml file
            DOMSource source = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(fpath);

            // Set up transformer
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Properties outFormat = new Properties();
            outFormat.setProperty(OutputKeys.INDENT, "yes");
            outFormat.setProperty(OutputKeys.METHOD, "xml");
            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            outFormat.setProperty(OutputKeys.VERSION, "1.0");
            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperties(outFormat);
            transformer.transform(source, streamResult);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }
    }

	// Used for testing only
	@SuppressWarnings("unused")
	private static String readAll(String fpath) {
		File xmlFile = new File(fpath);
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(xmlFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		String text = "";
		String tmpString;
		
		try {
			while((tmpString = reader.readLine()) != null) {
				text += tmpString + '\n';
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}
}
