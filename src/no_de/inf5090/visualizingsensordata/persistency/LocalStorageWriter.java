package no_de.inf5090.visualizingsensordata.persistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * This class logs all changes in sensors. When writeXML is executed, the readings will be written to file in xml format
 * 
 * @author aage et al.
 *
 */
public class LocalStorageWriter extends DataCollector {
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
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
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
