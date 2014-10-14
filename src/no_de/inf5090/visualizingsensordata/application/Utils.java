package no_de.inf5090.visualizingsensordata.application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Utils {
    /**
     * Get a new Document-instance
     */
    public static Document getXmlDocumentInstance() {
        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * Get String-representation for Date-object according to ISO-8601 with milliseconds
     */
    public static String getDateString(Date date) {
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
        d.setTimeZone(TimeZone.getTimeZone("UTC"));
        return d.format(date);
    }
}