package no_de.inf5090.visualizingsensordata.persistency;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observer;
import java.util.zip.GZIPOutputStream;

import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class SnapshotWriter extends AsyncTask<byte[], Void, Void>{
	private Observer snapshotObserver; 
	private int jpegQuality = 50;
	private int photoWidth = 320;
	private int photoHeight = 240;
    /**
     * Constructor: initiates snapshotwriter
     */
    public SnapshotWriter(Observer observer) {
    	snapshotObserver = observer;
    }

    @Override
    protected Void doInBackground(byte[]... data) {
        /*try {
        	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        	try{
                 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                 gzipOutputStream.write(data[0]);
                 gzipOutputStream.close();
             } catch(IOException e){
                 throw new RuntimeException(e);
             }*/
             // System.out.printf("Compression ratio %f\n", (1.0f * data.length/byteArrayOutputStream.size()));
             
         	//String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    	    // String encodedImage = Base64.encodeToString(data[0], Base64.DEFAULT);
        	
        	
        	
        	Bitmap image = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
        	int ratio = image.getHeight() / photoHeight;        	
        	image = Bitmap.createScaledBitmap(image, image.getWidth() / ratio, image.getHeight() / ratio, true);
        	
            //String fPath = VideoCapture.appDir.getPath() + "/" + System.currentTimeMillis() + ".jpg";
           // FileOutputStream fos = new FileOutputStream(fPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
          /*fos.write(data[0]);
           fos.write(out.toByteArray());
            fos.flush();
            fos.close();
            
            snapshotData.add(fPath); // add full path of the just taken snapshot to the queue*/
            String encodedImage = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        	snapshotObserver.update(null, encodedImage);
        	

            //Log.d("snap", "onPictureTaken - wrote bytes: " + data.length + " to " + fPath);
            Log.d("snap", "onPictureTaken - wrote bytes: " + data.length + " to SnapshotObserver");
        /*} catch (Exception e) {
            e.printStackTrace();
        }*/

        return null;
    }

}
