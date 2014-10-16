package no_de.inf5090.visualizingsensordata.domain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.userInterface.CameraPreview;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

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
		private int jpegQuality = 75;
		private int photoWidth = 256;
		private int photoHeight = 144;
		
		/** Original size of frame */
    	private Camera.Size mSize = mCameraHelper.getCamera().getParameters().getPreviewSize();

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
			im.compressToJpeg(r, jpegQuality, baos);
			
			// Convert JPEG to Bitmap
			byte[] jpegImage = baos.toByteArray();
			Bitmap image = BitmapFactory.decodeByteArray(jpegImage, 0, jpegImage.length);
			
			// Scale Bitmap
			int ratio = image.getHeight() / photoHeight;
            image = Bitmap.createScaledBitmap(image, image.getWidth() / ratio, image.getHeight() / ratio, true);

            // Convert it back to JPEG 
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 90, out);

			return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		}

		protected void onPostExecute(String encodedImage) {
			// Set this observable as changed
			setChanged();

			// Send the encoded image to all observers
			notifyObservers(encodedImage);
			
			/*
            Log.d("snap", "onPictureTaken - org: " + encodedImageTest.length() + 
           		 " enc: " + encodedImage.length() + 
           		 " diff: " + (encodedImageTest.length() - encodedImage.length()) +
           		 " rat: " + (encodedImageTest.length() / (encodedImage.length()*1.0)));
			 */
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
    	private Handler mHandler = new Handler(Looper.getMainLooper());
    	private Camera.Size mSize = mCameraHelper.getCamera().getParameters().getPreviewSize();
		
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
    						mCameraHelper.getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback() {

    							@Override
    							public void onPreviewFrame(byte[] data, Camera camera) {
    								// Do converting and scaling in a background thread
                                    new SnapshotProcessor().execute(data);
    							}
    						});
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
