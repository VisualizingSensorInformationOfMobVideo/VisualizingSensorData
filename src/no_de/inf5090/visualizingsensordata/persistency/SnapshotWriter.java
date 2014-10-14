package no_de.inf5090.visualizingsensordata.persistency;

import java.io.ByteArrayOutputStream;
import java.util.Observer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class SnapshotWriter extends AsyncTask<byte[], Void, Void>{
	/** The observer to send snapshots to */
	private Observer snapshotObserver;
	
	private int jpegQuality = 50;
	private int photoWidth = 256;
	private int photoHeight = 144;
	
    /**
     * Constructor: initiates snapshotwriter
     */
    public SnapshotWriter(Observer snapshotObserver) {
    	this.snapshotObserver = snapshotObserver;
    }

    @Override
    protected Void doInBackground(byte[]... data) {
        	
        	Bitmap image = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
        	int ratio = image.getHeight() / photoHeight;        	
        	image = Bitmap.createScaledBitmap(image, image.getWidth() / ratio, image.getHeight() / ratio, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
            String encodedImage = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        	snapshotObserver.update(null, encodedImage);
        	
            //Log.d("snap", "onPictureTaken - wrote bytes: " + data.length + " to " + fPath);
            Log.d("snap", "onPictureTaken - wrote bytes: " + data.length + " to SnapshotObserver");
            
    		/*
                 Log.d("snap", "onPictureTaken - org: " + encodedImageTest.length() + 
                		 " enc: " + encodedImage.length() + 
                		 " diff: " + (encodedImageTest.length() - encodedImage.length()) +
                		 " rat: " + (encodedImageTest.length() / (encodedImage.length()*1.0)));
    		 */
        return null;
    }
}
