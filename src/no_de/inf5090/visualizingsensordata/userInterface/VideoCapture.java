package no_de.inf5090.visualizingsensordata.userInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.GPSTracker;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.persistency.GPXWriter;
import no_de.inf5090.visualizingsensordata.persistency.SnapshotWriter;
import no_de.inf5090.visualizingsensordata.transmission.SnapshotTransmission;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class VideoCapture extends Activity {

	private Camera myCamera;
	private MyCameraSurfaceView myCameraSurfaceView;
	private MediaRecorder mediaRecorder;
	private Context context;
	private String currentFileName;
	private GPSTracker gpsTracker;
	Button myButton;
	SurfaceHolder surfaceHolder;
	boolean recording;
	Thread t = null;
	private volatile boolean takingSnapshot; // Used by snapshot feature
	private static int snapshotCounter = 0; // Used by snapshot feature
	private int numOfSnapshots = 2;
	private static int delay =  2000;
	private SnapshotTransmission snapshotTransmission;
	public static boolean sendingSnapshot;
	
	// singleton
	private static VideoCapture self;
	
	public static File appDir; // Application directory
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContext(this);
		recording = false;
		setContentView(R.layout.main);

		myCamera = getCameraInstance();
		if (myCamera == null) {
			Toast.makeText(VideoCapture.this, "Fail to get Camera",
					Toast.LENGTH_LONG).show();
		}
		
		myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
		FrameLayout myCameraPreview = (FrameLayout) findViewById(R.id.videoview);
		myCameraPreview.addView(myCameraSurfaceView);

		myButton = (Button) findViewById(R.id.mybutton);
		myButton.setOnClickListener(myButtonOnClickListener);
		//myButton.setEnabled(false);
		myButton.setEnabled(true);
		
		// init app Dir
		String root = Environment.getExternalStorageDirectory().toString();
		appDir  = new File(root + "/VSD");    
        appDir.mkdirs();
    	    	
    	gpsTracker = new GPSTracker(context);
    	
    	// set as singleton
    	setSelf(this);
	}
	
	public void enableButton(){
		myButton.setEnabled(true);
	}
	
	// Start activity
	public void onGraphButtonClicked(View v){
    	Intent intent = new Intent(this, GraphDrawActivity.class);
    	startActivity(intent);		
	}

	@SuppressLint("NewApi")
	Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {
		@SuppressLint("NewApi")
		public void onClick(View v) {
			if (recording) {
				stopSnapshot();
				stopSendingSnapshot();
				
				// Write XML
				GPXWriter writer = new GPXWriter();
				writer.writeGPXFileForData(currentFileName, gpsTracker.getTrack());

				// Camera stuff
				mediaRecorder.stop(); // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object
				recording = false;
				myButton.setText("Record");
				
				// Stop recording sensor data
		        FragmentManager fragmentManager = getFragmentManager();
		        SensorListFragment sensorDataFragment = (SensorListFragment)fragmentManager.findFragmentById(R.id.sensorListFragment);
		        sensorDataFragment.stopPersistingSensorData(currentFileName);
			    myCamera.startPreview();
			} else {
				
				// create a new connection to the server
				// TODO create a variable with webserver url
				snapshotTransmission = new SnapshotTransmission("http://example.com"); 
				
				
				// Mark last recording start
				Utils.lastRecordingStar = new Date();
					
				// --- Camera Stuff
				// Release Camera before MediaRecorder start
				releaseCamera();
				if (!prepareMediaRecorder()) {
					Toast.makeText(VideoCapture.this,
							"Fail in prepareMediaRecorder()!\n - Ended -",
							Toast.LENGTH_LONG).show();
					finish();
				}
				
				// reset the track list
				gpsTracker.startNewTrack();
				
				mediaRecorder.start();
				recording = true;
				myButton.setText("Stop");

				// Start recording sensor data
		        FragmentManager fragmentManager = getFragmentManager();
		        SensorListFragment sensorDataFragment = (SensorListFragment)fragmentManager.findFragmentById(R.id.sensorListFragment);
		        sensorDataFragment.startPersistingSensorData();
		        
		        startSnapshot();
		        //myCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		        myCamera.takePicture(null, null, jpegCallback);
		        
		        startSendingSnapshot();
		        
		       /*Thread t = new Thread(new Runnable() { 
		        	public void run() {
		        		final long time = 1000;
		        		while(takingSnapshot) {
		        			myCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		        			try {
								Thread.sleep(time);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		        		}
		        		
		        		
		        	}
		        });
		        t.start();*/
			}
		}
	};
	
	private Camera getCameraInstance() {
		// TODO Auto-generated method stub
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private boolean prepareMediaRecorder() {
		myCamera = getCameraInstance();
		mediaRecorder = new MediaRecorder();

		myCamera.unlock();
		mediaRecorder.setCamera(myCamera);

		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		mediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", java.util.Locale.getDefault()) ;
		currentFileName = dateFormat.format(date);
		mediaRecorder.setOutputFile(appDir.getPath()+"/"+currentFileName+".mp4");
		//mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
		//mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M

		mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder()
				.getSurface());

		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			return false;
		}
		return true;
 
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();		
		//TODO: Resume Camera/Media recorder.
		gpsTracker.startUsingGPS();		
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	protected void onPause() {
		super.onPause();
		//releaseMediaRecorder(); // if you are using MediaRecorder, release it
		//releaseCamera(); // release the camera immediately on pause event
		gpsTracker.stopUsingGPS();
	}

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			myCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (myCamera != null) {
			myCamera.release(); // release the camera for other applications
			myCamera = null;
		}
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public class MyCameraSurfaceView extends SurfaceView implements
			SurfaceHolder.Callback {

		private SurfaceHolder mHolder;
		private Camera mCamera;

		public MyCameraSurfaceView(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			// Are we really going to support such old phones? Is GPS + Good camera even a given then?
			//mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// make any resize, rotate or reformatting changes here

			// start preview with new settings
			try {
				//mCamera.setPreviewDisplay(mHolder);
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();

			} catch (Exception e) {
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}
	}

	public File getAppDir() {
		return appDir;
	}

	public void setAppDir(File appDir) {
		VideoCapture.appDir = appDir;
	}
	
	/**
	 * Toggles the visibility of the sensor warnings when the toggle button is pressed.
	 * @param view	The toggle button view.
	 */
	public void onToggleClicked(View view) {
		View sensorList = findViewById(R.id.row_sensor_data);
		
		if (((ToggleButton) view).isChecked() == false) {
			sensorList.setVisibility(View.INVISIBLE);
		} else {
			sensorList.setVisibility(View.VISIBLE);
		}
	}

	public static VideoCapture getSelf() {
		return self;
	}

	public static void setSelf(VideoCapture self) {
		VideoCapture.self = self;
	}
	

	private void stopSnapshot() { takingSnapshot = false; }
	private void startSnapshot() { takingSnapshot = true; }
	private void stopSendingSnapshot() { sendingSnapshot = false; }
	private void startSendingSnapshot() { sendingSnapshot = true; }

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d("snap", "onShutter");
		}
	};
	
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("snap", "onPictureTaken - raw");
		}
	};
	
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			
			if (!takingSnapshot) {
				// The user has stopped recording
				return; 
			}
			
			myCamera.startPreview(); // to avoid preview freezing after taking a pic
			
			new SnapshotWriter().execute(data);
			Log.d("snap", "onPictureTaken - jpeg");	
			snapshotTransmission.send_snapshot();
			if (sendingSnapshot) {
				snapshotTransmission.send_snapshot();
		    }
			snapshotCounter++;
			if (snapshotCounter <= numOfSnapshots) {
	            Thread thread = new Thread() {
	                @Override
	                public void run() {
	                    try {
	                        sleep(delay);
	                        myCamera.takePicture(null, null, jpegCallback); 
	                    }
	                    catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                }
	            };
	            thread.start();
	        }
			else return;
		}
	};
	
	
}
