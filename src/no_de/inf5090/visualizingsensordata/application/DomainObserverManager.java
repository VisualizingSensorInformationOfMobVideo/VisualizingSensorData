package no_de.inf5090.visualizingsensordata.application;

import android.app.Activity;
import android.hardware.SensorManager;
import no_de.inf5090.visualizingsensordata.domain.*;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import java.util.ArrayList;
import java.util.Observer;

/**
 * Object for controlling/managing the observers in the domain package
 */
public class DomainObserverManager {
    /** Sensor list */
    private ArrayList<AbstractDomainObservable> sensors = new ArrayList<AbstractDomainObservable>();

    /**
     * Initialize sensors
     */
    public void initSensors() {
        AbstractDomainObservable sensor;
        SensorManager manager = (SensorManager) VideoCapture.getSelf().getSystemService(Activity.SENSOR_SERVICE);

        // acceleration sensor
        sensor = new AccelerationObserver(manager);
        sensors.add(sensor);

        // orientation sensor
        sensor = new RotationVectorObserver(manager);
        sensors.add(sensor);

        // movement sensor
        sensor = new LocationObserver(VideoCapture.getSelf().getContext());
        sensors.add(sensor);

        // brightness sensor
        sensor = new BrightnessObserver(manager);
        sensors.add(sensor);

        // snapshot sensor
        //sensor = new SnapshotObserver(VideoCapture.getSelf().getSnapshotObservable());
        //sensors.add(sensor);

        // start listening to sensors
        resumeSensors();
    }

    /**
     * Connect sensors to observers
     */
    public void connectSensors(Observer observer) {
        for (AbstractDomainObservable sensor: sensors) {
        	sensor.addObserver(observer);
        }
    }

    /**
     * Disconnect sensors from observers
     */
    public void disconnectSensors(Observer observer) {
        for (AbstractDomainObservable sensor: sensors) {
        	sensor.deleteObserver(observer);
        }
    }

    /**
     * Pause sensors (e.g. the application is paused)
     */
    public void pauseSensors() {
        for (AbstractDomainObservable sensor: sensors) {
            sensor.onPause();
        }
    }

    /**
     * Pause sensors (e.g. the application is paused)
     */
    public void resumeSensors() {
        for (AbstractDomainObservable sensor: sensors) {
            sensor.onResume();
        }
    }
    
    /**
     * Returns the first (by the order of how they got added) 
     * sensor that have the name given.
     * @param sensorName name of the wanted sensor
     * @return the sensor that match the given name, and null if no sensor is found
     */
    public AbstractDomainObservable getSensor(String sensorName) {
    	for (AbstractDomainObservable s : sensors) {
    		if (s.getName().equals(sensorName)) {
    			return s;
    		}
    	}
    	
    	return null;
    }
}
