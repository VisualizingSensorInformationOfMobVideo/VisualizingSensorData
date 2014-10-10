package no_de.inf5090.visualizingsensordata.userInterface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.application.SensorController;
import no_de.inf5090.visualizingsensordata.domain.*;
import no_de.inf5090.visualizingsensordata.persistency.SnapshotWriter;
import no_de.inf5090.visualizingsensordata.transmission.SnapshotTransmission;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import no_de.inf5090.visualizingsensordata.persistency.LocalStorageWriter;

import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class VideoCapture extends Activity {

    // Persistence instance
    private LocalStorageWriter mLocalStorageWriter;

    private Context mContext;
    private Button mRecordButton;

    // TODO: refactor snapshot stuff into SnapshotObserver and CameraHelper
    private volatile boolean takingSnapshot; // Used by snapshot feature
    private static int snapshotCounter = 0; // Used by snapshot feature
    private int numOfSnapshots = 2;
    private static int delay =  2000;
    private SnapshotTransmission snapshotTransmission;
    public static boolean sendingSnapshot;
    private int res_width = 640;
    private int res_height = 480;

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
    }

    /**
     * Set up camera stuff
     */
    private void initCameraStuff() {
        // create the helper, it will get and hold the real camera instance
        mCameraHelper = new CameraHelper();
        if (!mCameraHelper.hasCamera()) {
            Toast.makeText(VideoCapture.this, "Fail to get Camera", Toast.LENGTH_LONG).show();
            // FIXME: abort application?
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

        // create a new connection to the server
        // TODO create a variable with webserver url
        snapshotTransmission = new SnapshotTransmission("http://example.com");

        mCameraHelper.startRecording();
        mRecordButton.setText("Stop");

        // Start recording sensor data
        startPersistingSensorData();

        // TODO: refactor snapshot stuff into SnapshotObserver and CameraHelper
        startSnapshot();
        //myCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        mCameraHelper.getCamera().takePicture(null, null, jpegCallback);
        startSendingSnapshot();
    }

    /**
     * Stop recording
     */
    private void stopRecording() {
        stopSnapshot();
        stopSendingSnapshot();

        mCameraHelper.stopRecording();
        mRecordButton.setText("Record");

        // Stop recording sensor data
        stopPersistingSensorData();
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

    /**
     * Run when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Resume Camera/Media recorder.
        sensorController.resumeSensors();
    }

    /**
     * Run when the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        //releaseMediaRecorder(); // if you are using MediaRecorder, release it
        //releaseCamera(); // release the camera immediately on pause event

        sensorController.pauseSensors();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /*public File getAppDir() {
        return appDir;
    }

    public void setAppDir(File appDir) {
        VideoCapture.appDir = appDir;
    }*/

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

            // TODO: is this really needed?
            mCameraHelper.getCamera().startPreview(); // to avoid preview freezing after taking a pic

            new SnapshotWriter().execute(data);
            Log.d("snap", "onPictureTaken - jpeg");
            /*snapshotTransmission.send_snapshot();
            if (sendingSnapshot) {
                snapshotTransmission.send_snapshot();
            }*/
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
        }

        sensorController.connectSensors(mLocalStorageWriter);
        mLocalStorageWriter.startRecording();
    }

    /**
     * Stops and persists recording of sensor data
     */
    public void stopPersistingSensorData() {
        String correspondingFileName = getOutputFileName();
        sensorController.disconnectSensors(mLocalStorageWriter);
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
