package no_de.inf5090.visualizingsensordata.userInterface;

import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.domain.AccelerationSensorObserver;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
import no_de.inf5090.visualizingsensordata.domain.SpeedSensorObserver;
import no_de.inf5090.visualizingsensordata.persistency.SensorWriter;
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
	private RotationVectorObserver orientationSensor;
	
	// Persistence instance
	private SensorWriter sensorWriter;
	
	private View fragmentView;

	// List of view id's
	private enum ID {
		SENSORAZIMUTH,
		SENSORPITCH,
		SENSORROLL,
		SENSORSHAKE,
		SENSORSPEED;
	}
	
	//Warning toasts.
	private Toast shakeWarning;
	private Toast orientationWarning;
	private Toast speedWarning;
	
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

    	// Initiating orientation sensor
    	orientationSensor = new RotationVectorObserver((SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE));
    	orientationSensor.addObserver(this);
    	orientationSensor.addObserver(graphFragment);
    	
    	// Initiate row to handle my runtime added control
    	setUpAccelerationSensor();
    	setUpSpeedSensor();
    	setUpOrientationSensor();
    	
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
		orientationSensor.acquireResources();

	}

    @Override
	public void onPause() {
		super.onPause();
		accelerationSensor.onPause();
		speedSensor.stopUsingGPS();
		orientationSensor.freeResources();
	}
    
    /**
     * Starts recording of sensor data
     */
    public void startPersistingSensorData() {
    	accelerationSensor.addObserver(sensorWriter);
    	speedSensor.addObserver(sensorWriter);
    	//orientationSensor.addObserver(sensorWriter);
    }
    
    /**
     * Stops and persists recording of sensor data
     * @param currentFileName 
     */
    public void stopPersistingSensorData(String correspondingFileName) {
    	accelerationSensor.deleteObserver(sensorWriter);
    	speedSensor.deleteObserver(sensorWriter);
    	//orientationSensor.deleteObserver(sensorWriter);
    	sensorWriter.writeXML(VideoCapture.appDir.getPath()+"/"+correspondingFileName+"-sensor.xml");
    }
    
	public void update(Observable observable, Object data) {
		if (observable.equals(accelerationSensor)) {
			// Registered change in acceleration
			handleShakeEvent();
		} else if(observable.equals(speedSensor)) {
			handleSpeedEvent();
		} else if (observable.equals(orientationSensor)) {
			handleOrientionVectorObserverChanged();
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

    private void setUpOrientationSensor() {
    	ProgressBar bar;
    	
		// azimuth
    	bar = (ProgressBar)fragmentView.findViewById(R.id.azimuthBar);
    	bar.setId(ID.SENSORAZIMUTH.ordinal());
    	bar.setMax(100);
    	bar.setProgress(0);
    	
    	// pitch
    	bar = (ProgressBar)fragmentView.findViewById(R.id.pitchBar);
    	bar.setId(ID.SENSORPITCH.ordinal());
    	bar.setMax(100);
    	bar.setProgress(0);
    	
    	// roll
    	bar = (ProgressBar)fragmentView.findViewById(R.id.rollBar);
    	bar.setId(ID.SENSORROLL.ordinal());
    	bar.setMax(100);
    	bar.setProgress(0);
	}
	
	
	/**
	 * This method handles shake events and updates the progress bar to show if shaking is too bad.
	 */
	private void handleShakeEvent() {
		int shakeValue;
		ProgressBar progress_shake = (ProgressBar) fragmentView.findViewById(ID.SENSORSHAKE.ordinal());
		
		try {
			shakeValue = (int) (Math.abs(accelerationSensor.getShake()/10) * 100);
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
	private void handleOrientionVectorObserverChanged() {
		ProgressBar bar;
		int val;
		
		// azimuth
		bar = (ProgressBar)fragmentView.findViewById(ID.SENSORAZIMUTH.ordinal());
		val = (int) ((orientationSensor.getAzimuth() + Math.PI) / Math.PI / 2 * 100);
		bar.setProgress(val);
		
		// pitch
		bar = (ProgressBar)fragmentView.findViewById(ID.SENSORPITCH.ordinal());
		val = (int) (Math.abs(orientationSensor.getPitch()) / Math.PI * 200);
		bar.setProgress(val);
		bar.setBackgroundColor(val < 10 ? Color.GREEN : Color.RED);
		if (val >= 10) {
			if (this.fragmentView.isShown()) orientationWarning.show();
		}
		
		// roll
		bar = (ProgressBar)fragmentView.findViewById(ID.SENSORROLL.ordinal());
		val = (int) (Math.abs(orientationSensor.getRoll()) / Math.PI * 100);
		bar.setProgress(val);
		bar.setBackgroundColor(val < 10 ? Color.GREEN : Color.RED);
		if (val >= 10) {
			if (this.fragmentView.isShown()) orientationWarning.show();
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
