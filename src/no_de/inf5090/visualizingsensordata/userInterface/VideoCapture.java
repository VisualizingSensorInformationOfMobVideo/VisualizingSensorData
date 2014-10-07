package no_de.inf5090.visualizingsensordata.userInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import android.hardware.SensorManager;
import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.GPSTracker;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AccelerationSensorObserver;
import no_de.inf5090.visualizingsensordata.domain.LogicalSensorObservable;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
import no_de.inf5090.visualizingsensordata.domain.SpeedSensorObserver;
import no_de.inf5090.visualizingsensordata.persistency.GPXWriter;

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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import no_de.inf5090.visualizingsensordata.persistency.SensorWriter;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class VideoCapture extends Activity {

    // Persistence instance
    private SensorWriter sensorWriter;

    private Camera myCamera;
	private MyCameraSurfaceView myCameraSurfaceView;
	private MediaRecorder mediaRecorder;
	private Context context;
	private String currentFileName;
	private GPSTracker gpsTracker;
	Button myButton;
	SurfaceHolder surfaceHolder;
	boolean recording;

    /**
     * The sensor controller
     */
    private SensorController sensorController;

    // singleton
	private static VideoCapture self;
	
	public static File appDir; // Application directory
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // set as singleton
        setSelf(this);

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

        // set up sensors
        sensorController = new SensorController();
        sensorController.initSensors();

        myButton = (Button) findViewById(R.id.mybutton);
		myButton.setOnClickListener(myButtonOnClickListener);
		myButton.setEnabled(false);
		//myButton.setEnabled(true);
		
		// init app Dir
		String root = Environment.getExternalStorageDirectory().toString();
		appDir  = new File(root + "/VSD");    
        appDir.mkdirs();
    	    	
    	gpsTracker = new GPSTracker(context);
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
				
				// Write XML
				GPXWriter writer = new GPXWriter();
				writer.writeGPXFileForData(currentFileName, gpsTracker.getTrack());

				// Camera stuff
				mediaRecorder.stop(); // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object
				recording = false;
				myButton.setText("Record");
				
				// Stop recording sensor data
		        stopPersistingSensorData(currentFileName);
				
			} else {
				
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
		        startPersistingSensorData();
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

        sensorController.resumeSensors();
		
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

        sensorController.pauseSensors();
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
				mCamera.setPreviewDisplay(mHolder);
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

    /**
     * Starts recording of sensor data
     */
    public void startPersistingSensorData() {
        // initiate SensorWriter
        if (sensorWriter == null) {
            sensorWriter = new SensorWriter();
            sensorController.connectSensors(sensorWriter);
        }

        sensorWriter.startRecording();
    }

    /**
     * Stops and persists recording of sensor data
     */
    public void stopPersistingSensorData(String correspondingFileName) {
        sensorController.disconnectSensors(sensorWriter);
        sensorWriter.stopRecording();
        sensorWriter.writeXml(appDir.getPath()+"/"+correspondingFileName+"-sensor.xml");
    }

    /**
     * Let other parts of the application listen to logical sensors
     */
    public void connectSensors(Observer observer) {
        sensorController.connectSensors(observer);
    }

    /**
     * Our own sensor controller/manager
     */
    protected class SensorController {
        /** Sensor list */
        private ArrayList<LogicalSensorObservable> sensors = new ArrayList<LogicalSensorObservable>();

        /**
         * Initialize sensors
         */
        protected void initSensors() {
            LogicalSensorObservable sensor;
            SensorManager manager = (SensorManager) VideoCapture.getSelf().getSystemService(Activity.SENSOR_SERVICE);

            // acceleration sensor
            sensor = (LogicalSensorObservable) new AccelerationSensorObserver(manager);
            sensors.add(sensor);

            // orientation sensor
            sensor = (LogicalSensorObservable) new RotationVectorObserver(manager);
            sensors.add(sensor);

            // movement sensor
            //sensor = (LogicalSensorObservable) new SpeedSensorObserver(VideoCapture.getSelf().getContext());
            //sensors.add(sensor);

            // start listening to sensors
            resumeSensors();
        }

        /**
         * Connect sensors to observers
         */
        protected void connectSensors(Observer observer) {
            for (LogicalSensorObservable sensor: sensors) {
                sensor.addObserver(observer);
            }
        }

        /**
         * Disconnect sensors from observers
         */
        protected void disconnectSensors(Observer observer) {
            for (LogicalSensorObservable sensor: sensors) {
                sensor.deleteObserver(observer);
            }
        }

        /**
         * Pause sensors (e.g. the application is paused)
         */
        protected void pauseSensors() {
            for (LogicalSensorObservable sensor: sensors) {
                sensor.onPause();
            }
        }

        /**
         * Pause sensors (e.g. the application is paused)
         */
        protected void resumeSensors() {
            for (LogicalSensorObservable sensor: sensors) {
                sensor.onResume();
            }
        }
    }
}
