package no_de.inf5090.visualizingsensordata.persistency;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import no_de.inf5090.visualizingsensordata.domain.SnapshotData;

import android.graphics.Bitmap.CompressFormat;

public class SnapshotWriter {
	
	Queue<SnapshotData> snapshotData;
	
	/**
	 * Constructor: initiates snapshotwriter
	 */
	public SnapshotWriter() {
		snapshotData = new LinkedList<SnapshotData>();		
	}
	
	public void writeBitmap(String path, SnapshotData snapshotData) {
		try {
			FileOutputStream fos = new FileOutputStream(path + "/" + snapshotData.getName() + ".jpg");
			snapshotData.getBitmap().compress(CompressFormat.JPEG, 75, fos);
		    try {
				fos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException  e) {
		}
	}

}
