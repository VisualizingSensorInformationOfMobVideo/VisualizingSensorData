package no_de.inf5090.hqvideocapture.userInterface;

import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.hqvideocapture.R;
import no_de.inf5090.hqvideocapture.domain.AccelerationSensorObserver;
import no_de.inf5090.hqvideocapture.domain.RotationSensorObserver;
import no_de.inf5090.hqvideocapture.domain.SpeedSensorObserver;
import no_de.inf5090.hqvideocapture.domain.TiltSensorObserver;
import no_de.inf5090.hqvideocapture.persistency.SensorWriter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * This fragment implements a list of sensor inputs. All sensor readings will be visually displayed.
 * @author aage
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SensorListFragment extends Fragment implements Observer {
	
	// Sensor instances
	private SpeedSensorObserver speedSensor;
	private AccelerationSensorObserver accelerationSensor;
	private RotationSensorObserver rotationSensor;
	private TiltSensorObserver tiltSensor;
	
	// Persistence instance
	private SensorWriter sensorWriter;
	
	private View fragmentView;

	// List of view id's
	private enum ID {
		SENSORROTATE,
		SENSORTILT,
		SENSORINCLINATION,
		SENSORSHAKE,
		SENSORSPEED;
	}
	
	//Warning toasts.
	private Toast shakeWarning;
	private Toast orientationWarning;
	private Toast speedWarning;
	
	private boolean rotationError = false;
	private boolean tiltError = false;
	
    @SuppressLint("ShowToast")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	// Get fragment
    	fragmentView = inflater.inflate(R.layout.sensor_data_fragment, container, false);

    	FragmentManager fragmentManager = getFragmentManager();
        SensorDataGraphFragment graphFragment = (SensorDataGraphFragment)fragmentManager.findFragmentById(R.id.graphFragment);
        
        // Initiating acceleration sensor.
        accelerationSensor = new AccelerationSensorObserver((SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE));
    	accelerationSensor.addObserver(this);
    	accelerationSensor.addObserver(graphFragment);
    	
    	speedSensor = new SpeedSensorObserver(inflater.getContext());
    	speedSensor.addObserver(this);
    	speedSensor.addObserver(graphFragment);
    	
    	// initiating tilt sensor
    	tiltSensor = new TiltSensorObserver((SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE));
    	tiltSensor.addObserver(this);
    	tiltSensor.addObserver(graphFragment);

    	// Initiating rotation sensor
    	rotationSensor = new RotationSensorObserver((SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE));
    	rotationSensor.addObserver(this);
    	rotationSensor.addObserver(graphFragment);
    	
    	// Initiate row to handle my runtime added control
    	setUpAccelerationSensor();
    	setUpSpeedSensor();
    	setUpTiltSensor();
    	setUpRotationSensor();
    	
    	// Initiate SensorWriter
    	sensorWriter = new SensorWriter();
    	
		// Create warning messages.
    	shakeWarning = Toast.makeText(getActivity(), "Reduce shaking!", Toast.LENGTH_SHORT);
    	orientationWarning = Toast.makeText(getActivity(), "Keep camera straight!", Toast.LENGTH_SHORT);
    	speedWarning = Toast.makeText(getActivity(), "Walk slower!", Toast.LENGTH_SHORT);
    	
    	return fragmentView;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		accelerationSensor.onResume();
		speedSensor.startUsingGPS();
		tiltSensor.acquireResources();
		rotationSensor.acquireResources();

	}

    @Override
	public void onPause() {
		super.onPause();
		accelerationSensor.onPause();
		speedSensor.stopUsingGPS();
		tiltSensor.freeResources();
		rotationSensor.freeResources();
	}
    
    /**
     * Starts recording of sensor data
     */
    public void startPersistingSensorData() {
    	accelerationSensor.addObserver(sensorWriter);
    	speedSensor.addObserver(sensorWriter);
    	tiltSensor.addObserver(sensorWriter);
    	rotationSensor.addObserver(sensorWriter);
    }
    
    /**
     * Stops and persists recording of sensor data
     * @param currentFileName 
     */
    public void stopPersistingSensorData(String correspondingFileName) {
    	accelerationSensor.deleteObserver(sensorWriter);
    	speedSensor.deleteObserver(sensorWriter);
    	tiltSensor.deleteObserver(sensorWriter);
    	rotationSensor.deleteObserver(sensorWriter);
    	sensorWriter.writeXML(VideoCapture.appDir.getPath()+"/"+correspondingFileName+"-sensor.xml");
    }
    
	public void update(Observable observable, Object data) {
		if (observable.equals(accelerationSensor)) {
			// Registered change in acceleration
			handleShakeEvent();
		} else if(observable.equals(speedSensor)) {
			handleSpeedEvent();
		} else if(observable.equals(tiltSensor)) {
			handleTiltSensorObserverChanged();
		} else if(observable.equals(rotationSensor)) {
			handleRotationSensorObserverChanged();
		}
	}

	// Private functions
	private void setUpAccelerationSensor() {    	
    	// Bind progress bar to shake sensor
    	ProgressBar progress_shake = (ProgressBar)fragmentView.findViewById(R.id.shakeBar);
    	progress_shake.setId(ID.SENSORSHAKE.ordinal());
    	progress_shake.setMax(50);
    	progress_shake.setProgress(0);
	}

	private void setUpSpeedSensor() {
    	// Add progressbar to new row. Should probably be done in XML file instead.
    	ProgressBar progress_speed = (ProgressBar)fragmentView.findViewById(R.id.speedBar);
    	progress_speed.setId(ID.SENSORSPEED.ordinal());
    	progress_speed.setMax(25);
    	progress_speed.setProgress(0);
	}

    private void setUpTiltSensor() {
    	// bind progress bar to orientation sensor
    	ProgressBar progress_tilt = (ProgressBar)fragmentView.findViewById(R.id.tiltBar);
    	progress_tilt.setId(ID.SENSORTILT.ordinal());
    	progress_tilt.setMax(20);
    	progress_tilt.setProgress(0);
	}

    private void setUpRotationSensor() {
    	// Add progress bar for rotate
    	ProgressBar progress_rotate = (ProgressBar)fragmentView.findViewById(R.id.rotationBar);
    	progress_rotate.setId(ID.SENSORROTATE.ordinal());
    	progress_rotate.setMax(20);
    	progress_rotate.setProgress(0);
	}

	
	/**
	 * This method handles shake events and updates the progress bar to show if shaking is too bad.
	 */
	private void handleShakeEvent() {
		int shakeValue;
		ProgressBar progress_shake = (ProgressBar) fragmentView.findViewById(ID.SENSORSHAKE.ordinal());
		
		try {
			shakeValue = (int) (Math.abs(accelerationSensor.getShake()) * 100);
		} catch (Exception e) {
			e.printStackTrace();
			shakeValue = 0;
		}
	
		progress_shake.setProgress(shakeValue);
		if (shakeValue < 30) {	// Warning treshold.
			progress_shake.setBackgroundColor(Color.GREEN);
		} else { 
			progress_shake.setBackgroundColor(Color.RED);
			if (this.fragmentView.isShown()) shakeWarning.show();
		}
	}
	
	/*
	 * Visually presents the orientation of the device
	 */
	private void handleTiltSensorObserverChanged() {
		ProgressBar progress_tilt = (ProgressBar)fragmentView.findViewById(ID.SENSORTILT.ordinal());
		int tiltValue;
		try {
			tiltValue = (int)((TiltSensorObserver.normalizeRadianAngle(tiltSensor.getPitch()) * 100));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tiltValue = 0;
		}
		progress_tilt.setProgress(tiltValue);
		if (tiltValue < 10) {
			progress_tilt.setBackgroundColor(Color.GREEN);
			tiltError = false;
		} else {
			if(tiltError) {
				progress_tilt.setBackgroundColor(Color.RED);
				if (this.fragmentView.isShown()) orientationWarning.show();
			}
			
			tiltError = true;
		}
	}

	/*
	 * Visually presents the orientation of the device
	 */
	private void handleRotationSensorObserverChanged() {
		ProgressBar progress_rotate = (ProgressBar)fragmentView.findViewById(ID.SENSORROTATE.ordinal());
		int rotateValue;
		try {
			rotateValue = (int)((RotationSensorObserver.normalizeRadianAngle(rotationSensor.getRoll()) * 100));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rotateValue = 0;
		}
		progress_rotate.setProgress(rotateValue);
		if (rotateValue < 10) {
			progress_rotate.setBackgroundColor(Color.GREEN);
			rotationError = false;

		} else { 
			if(rotationError) {
				progress_rotate.setBackgroundColor(Color.RED);
				if (this.fragmentView.isShown()) orientationWarning.show();
			}
			rotationError = true;
		}
	}

	/**
	 * This method handles speed events and updates the progress bar to show if speed is to high.
	 */
	private void handleSpeedEvent() {
		int speedValue;
		ProgressBar progress_speed = (ProgressBar) fragmentView.findViewById(ID.SENSORSPEED.ordinal());
		
		try {
			speedValue = (int) (speedSensor.getSpeed() * 100);
		} catch (Exception e) {
			e.printStackTrace();
			speedValue = 0;
		}
	
		progress_speed.setProgress(speedValue>0?speedValue:0);
		if(speedValue < 15) {
			progress_speed.setBackgroundColor(Color.GREEN);
		} else { 
			progress_speed.setBackgroundColor(Color.RED);
			if (this.fragmentView.isShown()) speedWarning.show();
		}
	}
}
