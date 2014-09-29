package no_de.inf5090.visualizingsensordata.persistency;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import org.w3c.dom.Document;
import android.location.Location;
import android.util.Log;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.util.Properties;
import javax.xml.transform.OutputKeys;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
//http://de.wikipedia.org/wiki/GPS_Exchange_Format

public class GPXWriter {

	public GPXWriter() {

	
	}
	/**
	 * This funtion iterates through the array list and writes the points into a gpx file
	 * @param correspondingFileName: path to the target GPX file
	 * @param track: ArrayList of Location points
	 */
	public void writeGPXFileForData(String correspondingFileName,ArrayList<Location> track){
		try {
			Log.d("GPS", "Wrote GPX file with name "+correspondingFileName);
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
	                      .newInstance();
	            DocumentBuilder documentBuilder = documentBuilderFactory
	                      .newDocumentBuilder();
	            Document document = documentBuilder.newDocument();

	            Element gpxElement = document.createElement("gpx");
	            gpxElement.setAttribute("version", "1.1");
	            gpxElement.setAttribute("creator", "HQVCApp");
	            gpxElement.setAttribute("xlmns", "http://www.topografix.com/GPX/1/1");
	            gpxElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	            document.appendChild(gpxElement);
	            
	            Element metaData = document.createElement("metadata");
	            gpxElement.appendChild(metaData);
	            
	            Element route = document.createElement("rte");
	            gpxElement.appendChild(route);
	            
	            for (Location loc : track) {
	            	Element rtept = document.createElement("rtept");
	            	rtept.setAttribute("lat", loc.getLatitude()+"");
	            	rtept.setAttribute("lon", loc.getLongitude()+"");
	            	rtept.setAttribute("dateTime", loc.getTime()+"");
	            	route.appendChild(rtept);
	    		}
	            
	            TransformerFactory factory = TransformerFactory.newInstance();
	            Transformer transformer = factory.newTransformer();
	            Properties outFormat = new Properties();
	            outFormat.setProperty(OutputKeys.INDENT, "yes");
	            outFormat.setProperty(OutputKeys.METHOD, "xml");
	            outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	            outFormat.setProperty(OutputKeys.VERSION, "1.0");
	            outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
	            transformer.setOutputProperties(outFormat);
	            DOMSource domSource = 
	            new DOMSource(document.getDocumentElement());
	            OutputStream output = new ByteArrayOutputStream();
	            StreamResult result = new StreamResult(output);
	            transformer.transform(domSource, result);
	            String xmlString = output.toString();
	            
	           // GET myDir
	            String fpath = VideoCapture.appDir.getPath()+"/"+correspondingFileName+".gpx";
	            File file = new File (fpath);
	            if (file.exists ()) file.delete (); 
	            try {
	                   FileOutputStream out = new FileOutputStream(file);
	                   OutputStreamWriter osw = new OutputStreamWriter(out); 

	                   // Write the string to the file
	                   osw.write(xmlString);

	                   /* ensure that everything is
	                    * really written out and close */
	                   osw.flush();
	                   osw.close();
	                   out.flush();
	                   out.close();

	            } catch (Exception e) {
	                   e.printStackTrace();
	            }


	        } catch (ParserConfigurationException e) {
	        } catch (TransformerConfigurationException e) {
	        } catch (TransformerException e) {
	        }
        
	   
	}
	
	
}
