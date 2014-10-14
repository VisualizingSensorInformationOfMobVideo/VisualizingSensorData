package no_de.inf5090.visualizingsensordata.domain;

import java.io.ByteArrayOutputStream;
import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.application.CameraHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

public class SnapshotSensor extends Observable { 

    /**
     * The camera helper (injected when sensor is created)
     */
    private CameraHelper mCameraHelper;
    
    /**
     * Observer of snapshots
     */
    private Observer mSnapshotObserver;

    private volatile boolean takingSnapshot; // Used by snapshot feature
    private boolean safeToTakePicture;
    private static long delay =  1000;
    private SnapshotLoopThread mThreadLoop; 
    

    public SnapshotSensor(CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
        mThreadLoop = new SnapshotLoopThread();
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
        (new Thread(mThreadLoop)).start();
    }
    
    /**
     * Take one snapshot
     */
    public void takeSnapshot() {
    	if (safeToTakePicture) {
    		mCameraHelper.getCamera().takePicture(null, null, jpegCallback);
    		safeToTakePicture = false;
    	}
    }

    /**
     * Callback-object to handle generated pictures and run another snapshot after some delay
     *
     * TODO: refactor this somewhere else and maybe merge with SnapshotWriter?
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {
    		Log.d("snap", "onPictureTaken before execute");

    		// TODO: is this really needed?
    		mCameraHelper.getCamera().startPreview(); // to avoid preview freezing after taking a pic

    		new SnapshotProcesser(mSnapshotObserver).execute(data);
    		Log.d("snap", "onPictureTaken after execute");
    	}
    };

	class SnapshotProcesser extends AsyncTask<byte[], Void, String>{
		/** The observer to send snapshots to */
		private Observer snapshotObserver;

		private int jpegQuality = 50;
		private int photoWidth = 256;
		private int photoHeight = 144;

		/**
		 * Constructor: initiates snapshotwriter
		 */
		public SnapshotProcesser(Observer snapshotObserver) {
			this.snapshotObserver = snapshotObserver;
		}

		@Override
		protected String doInBackground(byte[]... data) {
			//String encodedImageTest = Base64.encodeToString(data[0], Base64.DEFAULT);

			Bitmap image = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
			int ratio = image.getHeight() / photoHeight;        	
            image = Bitmap.createScaledBitmap(image, image.getWidth() / ratio, image.getHeight() / ratio, true);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
			
			return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		}

		protected void onPostExecute(String encodedImage) {
			hasChanged();
			notifyObservers(encodedImage);
			//snapshotObserver.update(null, encodedImage);
			/*
            Log.d("snap", "onPictureTaken - org: " + encodedImageTest.length() + 
           		 " enc: " + encodedImage.length() + 
           		 " diff: " + (encodedImageTest.length() - encodedImage.length()) +
           		 " rat: " + (encodedImageTest.length() / (encodedImage.length()*1.0)));
			 */
			safeToTakePicture = true;
		}
	}
	
    private class SnapshotLoopThread implements Runnable {
    	private Handler mHandler = new Handler(Looper.getMainLooper());
    	
    	public void run() {
    		while (takingSnapshot) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        takeSnapshot();
                    }
                });

                try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
}
