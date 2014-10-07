package no_de.inf5090.visualizingsensordata.persistency;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import no_de.inf5090.visualizingsensordata.domain.SnapshotData;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

public class SnapshotWriter extends AsyncTask<byte[], Void, Void>{
	
	//Queue<SnapshotData> snapshotData;
	public static Queue<String> snapshotData = new LinkedList<String>();
	/**
	 * Constructor: initiates snapshotwriter
	 */
	public SnapshotWriter() {
		//snapshotData = new LinkedList<SnapshotData>();	
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

	@Override
	protected Void doInBackground(byte[]... data) {
		try {	
			String fPath = VideoCapture.appDir.getPath() + "/" + System.currentTimeMillis() + ".jpg";
			FileOutputStream fos = new FileOutputStream(fPath);
			fos.write(data[0]);
			fos.flush();
			fos.close();
			
			snapshotData.add(fPath); // add full path of the just taken snaphot to the queue
			
			Log.d("snap", "onPictureTaken - wrote bytes: " + data.length + " to " + fPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
