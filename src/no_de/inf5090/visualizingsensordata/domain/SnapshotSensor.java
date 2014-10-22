package no_de.inf5090.visualizingsensordata.domain;

import java.io.ByteArrayOutputStream;
import java.util.Observable;

import no_de.inf5090.visualizingsensordata.application.CameraHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;


/**
 * Takes pictures/frames and sends them to observers 
 */
public class SnapshotSensor extends Observable { 
    /** The camera helper (injected when sensor is created) */
    private CameraHelper mCameraHelper;

    /** Variable that is true while recording, otherwise false */
    private volatile boolean takingSnapshot;
    
    /** False when a picture/frame is being taken/processed */
    private boolean safeToTakePicture;
    
    /** Delay between each picture/frame */
    private static long delay =  1000;

    public SnapshotSensor(CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
    }
    
    /**
     * Stop taking snapshots
     */
    public void stopSnapshot() {
        takingSnapshot = false;
        safeToTakePicture = false;
    }

    /**
     * Start taking snapshots
     */
    public void startSnapshot() {
        takingSnapshot = true;
        safeToTakePicture = true;
        (new SnapshotLoopThread()).start();
    }

	class SnapshotProcessor extends AsyncTask<byte[], Void, String>{
		private int jpegQuality = 70;
		private int photoHeight = 200;
		
		/** Original size of frame */
    	private Camera.Size mSize;

        /**
         * @param size is the size of a preview frame
         */
    	public SnapshotProcessor(Camera.Size size) {
    		this.mSize = size;
    	}

    	/**
    	 * Gets a frame as input, and converts this to a JPEG and then to a Bitmap, because it 
    	 * is easier to scale a Bitmap than a YUV file. When the image is converted and scaled, it
    	 * is converted back to JPEG, and  encoded with BASE64 before it is returned as a String.
    	 * 
    	 * @param data is a picture/frame in the ImageFormat.NV21 format. 
    	 */
		@Override
		protected String doInBackground(byte[]... data) {
			// Convert YUV image to JPEG
			YuvImage im = new YuvImage(data[0], ImageFormat.NV21, mSize.width, mSize.height, null);
			Rect r = new Rect(0, 0,mSize.width, mSize.height);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			im.compressToJpeg(r, 100, baos);

			// Convert JPEG to Bitmap
			byte[] jpegImage = baos.toByteArray();
			Bitmap image = BitmapFactory.decodeByteArray(jpegImage, 0, jpegImage.length);

			// Scale Bitmap
			int ratio = image.getHeight() / photoHeight;
            image = Bitmap.createScaledBitmap(image, image.getWidth() / ratio, image.getHeight() / ratio, true);

            // Convert it back to JPEG 
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);

			return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		}

		protected void onPostExecute(String encodedImage) {
			// Set this observable as changed
			setChanged();

			// Send the encoded image to all observers
			notifyObservers(encodedImage);

            // Finished with this snapshot, resume
			safeToTakePicture = true;
		}
	}
	
	/**
	 * This thread is in a loop grabbing preview frames, the time between each frame is 
	 * determined by a delay-variable. 
	 * 
	 * The grabbing is done by the camera, and must be done in the UI-thread, a Handler 
	 * is used for this. The calculations on each frame is done in an AsyncTask.
	 */
    private class SnapshotLoopThread extends Thread {
    	/** Handler used for scheduling a snapshot on the UI-thread */
    	private Handler mHandler = new Handler(Looper.getMainLooper());

    	@Override
    	public void run() {

    		// Only run while recording
    		while (takingSnapshot) {

    			// Do not take more pictures while the previous is being taken
    			if (safeToTakePicture) {
    				// Wait with more pictures until this is done
    				safeToTakePicture = false; 

    				// previewCallback must be run in UI-thread 
    				mHandler.post(new Runnable() {

    					@Override
    					public void run() {
    						Camera c = mCameraHelper.getCamera();

    						// Check if someone has stopped the camera
    						if (c != null) {
    							c.setOneShotPreviewCallback(new Camera.PreviewCallback() {

    								@Override
    								public void onPreviewFrame(byte[] data, Camera camera) {
    									// Do converting and scaling in a background thread
    									new SnapshotProcessor(camera.getParameters().getPreviewSize()).execute(data);
    								}
    							});
    						}
    					}
    				});
    			}

    			try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
}
