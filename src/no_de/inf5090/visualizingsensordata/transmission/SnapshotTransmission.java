package no_de.inf5090.visualizingsensordata.transmission;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import no_de.inf5090.visualizingsensordata.persistency.SnapshotWriter;

public class SnapshotTransmission {
	
	URL url;
	HttpURLConnection urlConnection = null;
	
	boolean check_connection() {
		//TODO
		return true;
	}
	
	public SnapshotTransmission(String url_name) {
		try {
			url = new URL (url_name);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			urlConnection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send_snapshot(){
		// read fpath of the first snapshot which is not sent yet
		// check if there is a snapshot we can send
		if (SnapshotWriter.snapshotData.isEmpty())
			return;
		
		String fpath = SnapshotWriter.snapshotData.poll();
		// load Bitmap
		Bitmap mbitmap = BitmapFactory.decodeFile(fpath);
		// Bitmap to byte []
		byte [] byte_snapshot = bitmapToByteArray(mbitmap);
		// check connection
		/*if (check_connection()) {
			
			// setup the request
			// sent a snapshot
			try {
				DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());
				try {
					request.write(byte_snapshot);
					// delete a snapshot
						File mfile = new File(fpath);
						mfile.delete();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				request.flush();
				request.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}*/
		
	}
	public static byte[] bitmapToByteArray(Bitmap bm) {
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);
        // Log.e("DBG", buffer.remaining()+""); -- Returns 0
        return buffer.array();
    }
	
}
