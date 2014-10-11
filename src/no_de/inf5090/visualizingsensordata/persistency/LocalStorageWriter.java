package no_de.inf5090.visualizingsensordata.persistency;

import java.io.*;

import no_de.inf5090.visualizingsensordata.transmission.TransmissionService;
import no_de.inf5090.visualizingsensordata.transmission.XmlTransmission;
import android.util.Log;

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
        File file = new File (fpath);
        if (file.exists ()) file.delete();

        try {
            FileWriter writer = new FileWriter(fpath);
            transformXml(writer);
            writer.close();
            
            // maybe a bit hackish, just for testing purpose right now
            XmlTransmission trans = new XmlTransmission(getXmlString());
            TransmissionService.getService().addTransmission(trans);
        } catch (IOException e) {
            Log.e("LocalStorageWriter", "IOException detected!");
        }
    }
}
