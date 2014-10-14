package no_de.inf5090.visualizingsensordata.userInterface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.application.SensorController;
import no_de.inf5090.visualizingsensordata.domain.LocationSensorObserver;
import no_de.inf5090.visualizingsensordata.domain.SnapshotObserver;
import no_de.inf5090.visualizingsensordata.persistency.LocalStorageWriter;
import no_de.inf5090.visualizingsensordata.persistency.RemoteDataPusher;
import no_de.inf5090.visualizingsensordata.persistency.SnapshotWriter;
import no_de.inf5090.visualizingsensordata.transmission.StopTransmission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class VideoCapture extends Activity {

    // Persistence instance
    private LocalStorageWriter mLocalStorageWriter;
    private RemoteDataPusher mRemoteDataPusher;

    private Context mContext;
    private Button mRecordButton;

    // TODO: refactor snapshot stuff into SnapshotObserver and CameraHelper
    private volatile boolean takingSnapshot; // Used by snapshot feature
    private static int snapshotCounter = 0; // Used by snapshot feature
    private int numOfSnapshots = 2;
    private static int delay =  2000;

    /**
     * The sensor controller
     */
    private SensorController sensorController;

    /**
     * The Camera helper
     */
    private CameraHelper mCameraHelper;

    /**
     * The Camera preview view
     */
    private CameraPreview mCameraPreview;

    /**
     * The current basename (without prefix) for the recording
     */
    private String mCurrentFileName;

    // singleton
    private static VideoCapture self;

    /**
     * Application directory
     */
    public static File appDir;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("VideoCapture", "onCreate");
        super.onCreate(savedInstanceState);

        // set as singleton
        setSelf(this);

        setContext(this);
        setContentView(R.layout.main);

        // do the camera magic
        initCameraStuff();

        // set up sensors
        sensorController = new SensorController();
        sensorController.initSensors();

        // set up the record button
        mRecordButton = (Button) findViewById(R.id.mybutton);
        mRecordButton.setOnClickListener(myButtonOnClickListener);
        mRecordButton.setEnabled(true);

        // init app Dir
        String root = Environment.getExternalStorageDirectory().toString();
        appDir  = new File(root + "/VSD");
        appDir.mkdirs();

        // show GPS-dialog if disabled
        if (!LocationSensorObserver.isGpsEnabled(this)) {
            LocationSensorObserver.showSettingsDialog(this);
        }
    }

    /**
     * Set up camera stuff
     */
    private void initCameraStuff() {
        // create the helper, it will get and hold the real camera instance
        mCameraHelper = new CameraHelper();
        if (!mCameraHelper.hasCamera()) {
            Toast.makeText(VideoCapture.this, "Fail to get Camera", Toast.LENGTH_LONG).show();
            VideoCapture.getSelf().finish(); //abort application
        }

        // create a camera surface for the preview and add to the view
        mCameraPreview = new CameraPreview(this, mCameraHelper);
        FrameLayout frame = (FrameLayout) findViewById(R.id.videoview);
        frame.addView(mCameraPreview);

        // make sure the camera helper knows the preview
        // it will make sure the video is previewed
        // TODO: this is not necessary, as the preview class will inject into the camera object itself
        mCameraHelper.setCameraPreview(mCameraPreview);
    }

    /**
     * Sets a new basename (without prefix) as the current output file
     */
    private void refreshOutputFileName() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", java.util.Locale.getDefault());
        mCurrentFileName = dateFormat.format(date);
    }

    /**
     * Get name of current output file
     */
    public String getOutputFileName() {
        return mCurrentFileName;
    }

    /**
     * Start recording
     */
    private void startRecording() {
        // create a new name for this recording
        refreshOutputFileName();

        mCameraHelper.startRecording();
        mRecordButton.setText("Stop");

        // Start recording sensor data
        startPersistingSensorData();

        // TODO: refactor snapshot stuff into SnapshotObserver and CameraHelper
        startSnapshot();
        mCameraHelper.getCamera().takePicture(null, null, jpegCallback);
    }

    /**
     * Stop recording (i.e. movie session)
     */
    private void stopRecording() {
        stopSnapshot();

        mCameraHelper.stopRecording();
        mRecordButton.setText("Record");

        // Stop recording sensor data
        stopPersistingSensorData();
        
        // send kill signal to server
        new Handler().post(new StopTransmission());
    }

	/**
     * Handle the record button
     */
    @SuppressLint("NewApi")
    Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {
        @SuppressLint("NewApi")
        public void onClick(View v) {
            if (mCameraHelper.isRecording()) {
                stopRecording();
            } else {
                startRecording();
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d("VideoCapture", "onDestroy");
        super.onDestroy();
        mCameraHelper.onDestroy();
    }

    /**
     * Run when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        mCameraHelper.onResume();
        mCameraPreview.startPreview();
        sensorController.resumeSensors();
    }

    /**
     * Run when the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stop any recording
        if (mCameraHelper.isRecording()) {
            stopRecording();
        }

        mCameraHelper.onPause();
        sensorController.pauseSensors();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Toggles the visibility of the sensor warnings when the toggle button is pressed.
     * @param view    The toggle button view.
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
     * Listen to location updates from GPS-sensor
     */
    public void locationUpdate(LocationSensorObserver sensor) {
        // on location change we can start record
        // TODO: our project really don't need a location, so we don't really need this
        mRecordButton.setEnabled(true);
    }


    // TODO: refactor snapshot stuff into SnapshotObserver and CameraHelper

    private void stopSnapshot() { takingSnapshot = false; }
    private void startSnapshot() { takingSnapshot = true; }

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            if (!takingSnapshot) {
                // The user has stopped recording
                return;
            }

            // TODO: is this really needed?
            mCameraHelper.getCamera().startPreview(); // to avoid preview freezing after taking a pic

            new SnapshotWriter((Observer) sensorController.getSensor(SnapshotObserver.NAME)).execute(data);
            Log.d("snap", "onPictureTaken - jpeg");
            
            snapshotCounter++;
            if (snapshotCounter <= numOfSnapshots) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(delay);
                            mCameraHelper.getCamera().takePicture(null, null, jpegCallback);
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

    /**
     * Starts recording of sensor data
     */
    public void startPersistingSensorData() {
        // initiate SensorWriter
        if (mLocalStorageWriter == null) {
            mLocalStorageWriter = new LocalStorageWriter();
            mLocalStorageWriter.setCameraHelper(mCameraHelper);
        }
        if (mRemoteDataPusher == null) {
            mRemoteDataPusher = new RemoteDataPusher();
            mRemoteDataPusher.setCameraHelper(mCameraHelper);
        }

        sensorController.connectSensors(mLocalStorageWriter);
        sensorController.connectSensors(mRemoteDataPusher);
        mLocalStorageWriter.startRecording();
        mRemoteDataPusher.startRecording();
    }

    /**
     * Stops and persists recording of sensor data
     */
    public void stopPersistingSensorData() {
        String correspondingFileName = getOutputFileName();
        sensorController.disconnectSensors(mLocalStorageWriter);
        sensorController.disconnectSensors(mRemoteDataPusher);
        mRemoteDataPusher.finish();
        mRemoteDataPusher = null;
        mLocalStorageWriter.stopRecording();
        mLocalStorageWriter.writeXml(appDir.getPath() + "/" + correspondingFileName + "-sensor.xml");
    }

    /**
     * Let other parts of the application listen to logical sensors
     */
    public void connectSensors(Observer observer) {
        sensorController.connectSensors(observer);
    }
}