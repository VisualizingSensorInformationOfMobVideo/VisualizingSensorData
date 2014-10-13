package no_de.inf5090.visualizingsensordata.persistency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

/**
 * This class logs all changes in sensors. When writeXML is executed, the
 * readings will be written to file in xml format
 *
 * @author aage et al.
 *
 */
public class LocalStorageWriter extends DataCollector {
	/**
	 * Generate XML-file and empty data set
	 */
	public void writeXml(String fpath) {
		File file = new File(fpath);
		if (file.exists())
			file.delete();

		try {
			FileWriter writer = new FileWriter(fpath);
			transformXml(writer);
			writer.close();
		} catch (IOException e) {
			Log.e("LocalStorageWriter", "IOException detected!");
		}
	}
}
