package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;
import java.util.Observer;

import android.hardware.Camera;
import android.util.Log;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;
import no_de.inf5090.visualizingsensordata.application.Utils;

import no_de.inf5090.visualizingsensordata.persistency.SnapshotWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SnapshotObserver extends LogicalSensorObservable implements Observer {
    private String encodedImage;

    /**
     * A the name for this sensor
     */
    public final static String NAME = "Snapshot";

    /**
     * Time of last update
     */
    private long mLastTime = System.currentTimeMillis();

    /**
     * Minimum delay between updates in milliseconds
     */
    private static final long MINIMUM_DELAY = 150;

    /**
     * The camera helper (injected when sensor is created)
     */
    private CameraHelper mCameraHelper;

    private volatile boolean takingSnapshot; // Used by snapshot feature
    private static int snapshotCounter = 0; // Used by snapshot feature
    private int numOfSnapshots = 2;
    private static int delay =  2000;

    public SnapshotObserver(CameraHelper cameraHelper) {
        mCameraHelper = cameraHelper;
    }

    @Override
    public void update(Observable observable, Object data) {
        // make sure the sensor don't flood; time since last update should be above threshold
        long now = System.currentTimeMillis();
        long elapsed = now - mLastTime;
        mLastTime = now;
        if (elapsed < MINIMUM_DELAY) return;

        /*
         * Do something with the data updated
         */
        encodedImage = (String) data;
        
        setChanged();
        notifyObservers(new LogicalSensorData(this));
    }

    @Override
    public void onPause() {
        // controlled directly from controller when recording stops
        //stopSnapshot();
    }

    @Override
    public void onResume() {
        // controlled directly from controller when recording start
        //startSnapshot();
    }

    /**
     * Stop taking snapshots
     */
    public void stopSnapshot() {
        takingSnapshot = false;

    }

    /**
     * Start taking snapshots
     */
    public void startSnapshot() {
        takingSnapshot = true;
        mCameraHelper.getCamera().takePicture(null, null, jpegCallback);
    }

    /**
     * Get the object itself (used by subclass)
     */
    protected SnapshotObserver getObj() {
        return this;
    }

    /**
     * Callback-object to handle generated pictures and run another snapshot after some delay
     *
     * TODO: refactor this somewhere else and maybe merge with SnapshotWriter?
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            if (!takingSnapshot) {
                // The user has stopped recording
                return;
            }

            // TODO: is this really needed?
            mCameraHelper.getCamera().startPreview(); // to avoid preview freezing after taking a pic

            new SnapshotWriter(getObj()).execute(data);
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

    @Override
	public String getName() {
		return NAME;
	}
    
    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
    	private String encodedImage;
    	
        protected LogicalSensorData(SnapshotObserver sensor) {
            super(sensor);
            this.encodedImage = sensor.encodedImage;
        }

        @Override
        public Element getXml(Document doc) {
            Element item = getBaseXml(doc);

            // actual sensor data
            Element elm = doc.createElement(xmlDataEntryName);
            elm.setAttribute("type", "base64data");
            elm.appendChild(doc.createTextNode(encodedImage));
            item.appendChild(elm);

            return item;
        }

        @Override
        public String getSensorName() {
            return getName();
        }
    }
}
