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
     * Instance for creating DOM
     */
    protected static Document document;

    public static int puEncodingFormat = 0;
    public static int puContainerFormat = 0;
    public static int puResolutionChoice = 0;

    public static Date lastRecordingStar = new Date();

    /**
     * Get Document-instance
     */
    public static Document getDocumentInstance() {
        if (document == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                document = docBuilder.newDocument();
            } catch (ParserConfigurationException e) {
                // TODO: this will cause a NullPointerException in current implementation
            }
        }
        return document;
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