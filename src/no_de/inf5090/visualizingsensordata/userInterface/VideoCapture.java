package no_de.inf5090.visualizingsensordata.userInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;
import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.application.DomainObserverManager;
import no_de.inf5090.visualizingsensordata.domain.LocationObserver;
import no_de.inf5090.visualizingsensordata.persistency.LocalStorageWriter;
import no_de.inf5090.visualizingsensordata.persistency.RemoteDataPusher;
import no_de.inf5090.visualizingsensordata.transmission.BaseTransmission;
import no_de.inf5090.visualizingsensordata.transmission.StopTransmission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.majorkernelpanic.streaming.Session;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class VideoCapture extends Activity implements Session.Callback, RtspClient.Callback {

    // Persistence instance
    private LocalStorageWriter mLocalStorageWriter;
    private RemoteDataPusher mRemoteDataPusher;

    private Context mContext;
    private Button mRecordButton;

    /**
     * Name of preference for remote host (see preferences.xml)
     */
    public final static String KEY_PREF_REMOTE_HOST = "remote_host";

    /**
     * Name of preference for enabling remote connection (see preferences.xml)
     */
    public final static String KEY_PREF_REMOTE_ENABLED = "remote_connect";

    /**
     * Name of preference for enabling local storage (see preferences.xml)
     */
    public final static String KEY_PREF_LOCAL_ENABLED = "local_storage";

    /**
     * The sensor controller
     */
    private DomainObserverManager domainObserverManager;

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

    public Session mSession;
    public RtspClient mClient;

    /**
     * Application directory
     */
    public static File appDir;

    /**
     * battery consumption
     */
    
    public void printBatteryLevel(String mString) {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        float scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = ((float)level / (float)scale) * 100.0f;
        
        try {
            FileWriter out = new FileWriter(new File(appDir, mString + ".txt"));
            out.write(String.valueOf(batteryPct));
            out.close();
        } catch (IOException e) {
        }
        Log.i(mString, " " + batteryPct);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("VideoCapture", "onCreate");
        super.onCreate(savedInstanceState);

        
        
        
        // set as singleton
        setSelf(this);

        setContext(this);
        setContentView(R.layout.main);

        // set default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // do the camera magic
        //initCameraStuff();

        // create a camera surface for the preview and add to the view
        mCameraPreview = new CameraPreview(this, mCameraHelper);
        FrameLayout frame = (FrameLayout) findViewById(R.id.videoview);
        frame.addView(mCameraPreview);

        //CamcorderProfile p = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //editor.putString(RtspServer.KEY_PORT, String.valueOf(1935));
        //editor.commit();

        mSession = SessionBuilder.getInstance()
                .setContext(this)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                //.setAudioQuality(new AudioQuality(8000, 16000))
                .setAudioQuality(new AudioQuality(48000, 128000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                //.setVideoQuality(new VideoQuality(640, 360))
                // default video: 176x144
                //.setVideoQuality(new VideoQuality(640, 480, 30, 1000000))
                .setVideoQuality(new VideoQuality(1280, 720, 30, 4000000)) // test 720 movie: 5 mbit
                //.setVideoQuality(new VideoQuality(1920, 1080, 1, 3000000)) // test 1080 movie: 8 mbit
                .setSurfaceView(mCameraPreview)
                .setPreviewOrientation(0)
                .setCallback(this)
                //.setDestination("37.191.203.206")
                //.setDestination("37.191.193.98")
                .setDestination("37.191.195.33")
                .build();
        //mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIARECORDER_API);

        //this.startService(new Intent(this, RtspServer.class));

        /*mClient = new RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);
        mClient.setCredentials("inf5090", "vsd");
        mClient.setServerAddress("dev1.hsw.no", 1935);
        mClient.setStreamPath("/live/test1");*/


        // set up sensors
        domainObserverManager = new DomainObserverManager();
        domainObserverManager.initSensors();

        // set up the record button
        mRecordButton = (Button) findViewById(R.id.mybutton);
        mRecordButton.setOnClickListener(myButtonOnClickListener);
        mRecordButton.setEnabled(true);

        // init app Dir
        String root = Environment.getExternalStorageDirectory().toString();
        appDir  = new File(root + "/VSD");
        appDir.mkdirs();

        // show GPS-dialog if disabled
        if (!LocationObserver.isGpsEnabled(this)) {
            LocationObserver.showSettingsDialog(this);
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
        printBatteryLevel("VideoCapture_startRecording_beginning");
        // create a new name for this recording
        refreshOutputFileName();

        mCameraHelper.startRecording();
        mRecordButton.setText("Stop");

        // Start recording sensor data
        startPersistingSensorData();
    }

    /**
     * Stop recording (i.e. movie session)
     */
    private void stopRecording() {
        mCameraHelper.stopRecording();
        mRecordButton.setText("Record");

        // Stop recording sensor data
        stopPersistingSensorData();
        printBatteryLevel("VideoCapture_stopRecording_end");
    }

	/**
     * Handle the record button
     */
    @SuppressLint("NewApi")
    Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {
        @SuppressLint("NewApi")
        public void onClick(View v) {
            mSession.start();
            /*
            if (mCameraHelper.isRecording()) {
                stopRecording();
            } else {
                if (!LocalStorageWriter.isLocalStorageAvailable() && !BaseTransmission.isTransferAvailable()) {
                    Toast.makeText(VideoCapture.this, "Neither local storage nor remote transmission is enabled. Cannot start recording. Change in settings.", Toast.LENGTH_LONG).show();
                } else {
                    startRecording();
                }
            }*/
        }
    };

    @Override
    protected void onDestroy() {
        Log.d("VideoCapture", "onDestroy");
        super.onDestroy();
        //mCameraHelper.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle selection in the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // open settings window
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Run when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        //mCameraHelper.onResume();
        //mCameraPreview.startPreview();
        domainObserverManager.resumeSensors();
    }

    /**
     * Run when the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stop any recording
        /*if (mCameraHelper.isRecording()) {
            stopRecording();
        }*/

        //mCameraHelper.onPause();
        domainObserverManager.pauseSensors();
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
    public void locationUpdate(LocationObserver sensor) {
        // on location change we can start record
        // TODO: our project really don't need a location, so we don't really need this
        mRecordButton.setEnabled(true);
    }

    /**
     * Starts recording of sensor data
     */
    public void startPersistingSensorData() {
        if (LocalStorageWriter.isLocalStorageAvailable()) {
            if (mLocalStorageWriter == null) {
                mLocalStorageWriter = new LocalStorageWriter();
                mLocalStorageWriter.setCameraHelper(mCameraHelper);
            }
            domainObserverManager.connectSensors(mLocalStorageWriter);
            mLocalStorageWriter.startRecording();
        }

        if (BaseTransmission.isTransferAvailable()) {
            if (mRemoteDataPusher == null) {
                mRemoteDataPusher = new RemoteDataPusher();
                mRemoteDataPusher.setCameraHelper(mCameraHelper);
            }
            domainObserverManager.connectSensors(mRemoteDataPusher);
            mRemoteDataPusher.startRecording();
        }
    }

    /**
     * Stops and persists recording of sensor data
     */
    public void stopPersistingSensorData() {
        if (mRemoteDataPusher != null) {
            domainObserverManager.disconnectSensors(mRemoteDataPusher);
            mRemoteDataPusher.finish();
            mRemoteDataPusher = null;

            // send StopTransmission to server
            new Handler().post(new StopTransmission());
        }

        if (mLocalStorageWriter != null) {
            String correspondingFileName = getOutputFileName();
            domainObserverManager.disconnectSensors(mLocalStorageWriter);
            mLocalStorageWriter.stopRecording();
            mLocalStorageWriter.writeXml(appDir.getPath() + "/" + correspondingFileName + "-sensor.xml");
        }
    }

    /**
     * Let other parts of the application listen to logical sensors
     */
    public void connectSensors(Observer observer) {
        domainObserverManager.connectSensors(observer);
    }

    /**
     * Get the current camera helper
     */
    public CameraHelper getCameraHelper() {
        return mCameraHelper;
    }

	public Observable getSnapshotObservable() {
		return mCameraHelper.getSnapshotSensor();
	}

    @Override
    public void onBitrateUpdate(long bitrate) {
        // Informs you of the bandwidth consumption of the streams
        Log.d("VideoCapture", "Bitrate: "+bitrate);
    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {

    }

    @Override
    public void onPreviewStarted() {
        //mClient.startStream();
    }

    @Override
    public void onSessionConfigured() {
        Log.d("VideoCapture", mSession.getSessionDescription());
        //mSession.start();
    }

    @Override
    public void onSessionStarted() {
        Log.d("VideoCapture", "Streaming session started.");

    }

    @Override
    public void onSessionStopped() {
        Log.d("VideoCapture", "Streaming session stopped.");
    }

    @Override
    public void onRtspUpdate(int message, Exception exception) {

    }
}
