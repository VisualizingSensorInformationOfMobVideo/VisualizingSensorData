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
import no_de.inf5090.visualizingsensordata.domain.SensorData;

/**
 * This class logs all changes in sensors. When writeXML is executed, the readings will be written to file in xml format
 * 
 * @author aage
 *
 */
public class SensorWriter implements Observer {

	List<SensorData> sensorData;
	
	/**
	 * Constructor: initiates sensorwriter
	 */
	public SensorWriter() {
		sensorData = new ArrayList<SensorData>();
		Utils.sensorDatas = new ArrayList<SensorData>();
	}
	
	/**
	 * Notification of a change in one of the sensors
	 * Changes are collected
	 */
	public void update(Observable observable, Object data) {
		if(data instanceof SensorData) {
			sensorData.add((SensorData) data);
			Utils.sensorDatas.add((SensorData) data);
		}
	}
	
	/**
	 * Stores all logged readings on a file in xml format
	 * @param correspondingFileName: name of file to store readings in
	 */
	public void writeXML(String fpath) {

        File file = new File (fpath);
        if (file.exists ()) file.delete (); 
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			
			// Root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("LogFile");
			doc.appendChild(rootElement);
			
			// App name
			Element appNameElement = doc.createElement("AppName");
			appNameElement.appendChild(doc.createTextNode("VisualizingSensorData"));
			rootElement.appendChild(appNameElement);

			Date date = new Date();
			// Time stamp
			Element timestapmElement = doc.createElement("Timestamp");
			timestapmElement.appendChild(doc.createTextNode(new SimpleDateFormat("yyyyMMddHHmmss.SSSS", java.util.Locale.getDefault()).format(date)));
			rootElement.appendChild(timestapmElement);
			
			//date
			Element appDateElement = doc.createElement("Date");
			appDateElement.appendChild(doc.createTextNode(new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)));
			rootElement.appendChild(appDateElement);
			
			//time
			Element appTimeElement = doc.createElement("Time");
			appTimeElement.appendChild(doc.createTextNode(new SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(date)));
			rootElement.appendChild(appTimeElement);
			
			for (SensorData logItem : sensorData) {
				// Receipts elements
				Element itemElement = doc.createElement("LogItem");
				rootElement.appendChild(itemElement);
				
				// Sensor type
				Element classElement = doc.createElement("SensorType");
				if(logItem.getSensor()!=null) {
					classElement.appendChild(doc.createTextNode(logItem.getSensor().getClass().getName()));
				}
				itemElement.appendChild(classElement);
				
				// Date for reading
				Element dateElement = doc.createElement("Date");					
				dateElement.appendChild(doc.createTextNode(new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(logItem.getTimestamp())));
				itemElement.appendChild(dateElement);
				
				// Time for reading
				Element timeElement = doc.createElement("Time");					
				timeElement.appendChild(doc.createTextNode(new SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(logItem.getTimestamp())));
				itemElement.appendChild(timeElement);
				
				// Reading
				Element readingElement = doc.createElement("Entry");
				readingElement.appendChild(doc.createTextNode(Float.toString(logItem.getValue())));
				itemElement.appendChild(readingElement);
			}
			
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
							
			// File saved!

        } catch (ParserConfigurationException e) {
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
