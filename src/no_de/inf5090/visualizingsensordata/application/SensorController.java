package no_de.inf5090.visualizingsensordata.application;

import android.app.Activity;
import android.hardware.SensorManager;
import no_de.inf5090.visualizingsensordata.domain.*;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import java.util.ArrayList;
import java.util.Observer;

/**
 * Our own sensor controller/manager
 */
public class SensorController {
    /** Sensor list */
    private ArrayList<LogicalSensorObservable> sensors = new ArrayList<LogicalSensorObservable>();

    /**
     * Initialize sensors
     */
    public void initSensors() {
        LogicalSensorObservable sensor;
        SensorManager manager = (SensorManager) VideoCapture.getSelf().getSystemService(Activity.SENSOR_SERVICE);

        // acceleration sensor
        sensor = new AccelerationSensorObserver(manager);
        sensors.add(sensor);

        // orientation sensor
        sensor = new RotationVectorObserver(manager);
        sensors.add(sensor);

        // movement sensor
        sensor = new LocationSensorObserver(VideoCapture.getSelf().getContext());
        sensors.add(sensor);

        // brightness sensor
        sensor = new BrightnessSensorObserver(manager);
        sensors.add(sensor);

        // snapshot sensor
        // TODO: the video object should be injected into the observer
        sensor = new SnapshotObserver();
        sensors.add(sensor);

        // start listening to sensors
        resumeSensors();
    }

    /**
     * Connect sensors to observers
     */
    public void connectSensors(Observer observer) {
        for (LogicalSensorObservable sensor: sensors) {
            sensor.addObserver(observer);
        }
    }

    /**
     * Disconnect sensors from observers
     */
    public void disconnectSensors(Observer observer) {
        for (LogicalSensorObservable sensor: sensors) {
            sensor.deleteObserver(observer);
        }
    }

    /**
     * Pause sensors (e.g. the application is paused)
     */
    public void pauseSensors() {
        for (LogicalSensorObservable sensor: sensors) {
            sensor.onPause();
        }
    }

    /**
     * Pause sensors (e.g. the application is paused)
     */
    public void resumeSensors() {
        for (LogicalSensorObservable sensor: sensors) {
            sensor.onResume();
        }
    }
}
