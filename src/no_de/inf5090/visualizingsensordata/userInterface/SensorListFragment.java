package no_de.inf5090.visualizingsensordata.userInterface;

import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.domain.AccelerationObserver;
import no_de.inf5090.visualizingsensordata.domain.LocationObserver;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * This fragment implements a list of sensor inputs. All sensor readings will be visually displayed.
 * @author aage et al.
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SensorListFragment extends Fragment implements Observer {
    private View fragmentView;

    //Warning toasts.
    /*private Toast shakeWarning;
    private Toast orientationWarning;
    private Toast speedWarning;*/

    //@SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get fragment
        fragmentView = inflater.inflate(R.layout.sensor_data_fragment, container, false);

        // set up progress bars to fit with our sensors
        setUpAccelerationSensor();
        setUpSpeedSensor();
        setUpOrientationSensor();

        // TODO: should we give feedback in our project?
        // Create warning messages.
        /*shakeWarning = Toast.makeText(getActivity(), "Reduce shaking!", Toast.LENGTH_SHORT);
        orientationWarning = Toast.makeText(getActivity(), "Keep camera straight!", Toast.LENGTH_SHORT);
        speedWarning = Toast.makeText(getActivity(), "Walk slower!", Toast.LENGTH_SHORT);*/

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);

        // connect to sensors so we can draw sensor data
        VideoCapture.getSelf().connectSensors(this);
    }

    /**
     * The update method will be called by our logical sensors we
     * are listening too, and will cause the progress bars to update
     */
    public void update(Observable observable, Object data) {
        // check for acceleration
        if (observable instanceof AccelerationObserver) {
            handleShakeEvent((AccelerationObserver.DomainData)data);
        }

        // check for speed
        else if (observable instanceof LocationObserver) {
            handleSpeedEvent((LocationObserver.DomainData)data);
        }

        // check for orientation
        else if (observable instanceof RotationVectorObserver) {
            handleOrientionVectorObserverChanged((RotationVectorObserver.DomainData)data);
        }
    }

    // Private functions
    private void setUpAccelerationSensor() {
        ProgressBar progress_shake = (ProgressBar)fragmentView.findViewById(R.id.shakeBar);
        progress_shake.setMax(50);
        progress_shake.setProgress(0);
    }

    private void setUpSpeedSensor() {
        ProgressBar progress_speed = (ProgressBar)fragmentView.findViewById(R.id.speedBar);
        progress_speed.setMax(25);
        progress_speed.setProgress(0);
    }

    private void setUpOrientationSensor() {
        ProgressBar bar;

        // azimuth
        bar = (ProgressBar)fragmentView.findViewById(R.id.azimuthBar);
        bar.setMax(100);
        bar.setProgress(0);

        // pitch
        bar = (ProgressBar)fragmentView.findViewById(R.id.pitchBar);
        bar.setMax(100);
        bar.setProgress(0);

        // roll
        bar = (ProgressBar)fragmentView.findViewById(R.id.rollBar);
        bar.setMax(100);
        bar.setProgress(0);
    }

    /**
     * This method handles shake events and updates the progress bar to show if shaking is too bad.
     */
    private void handleShakeEvent(AccelerationObserver.DomainData data) {
        ProgressBar bar = (ProgressBar) fragmentView.findViewById(R.id.shakeBar);
        int shakeValue = (int) (Math.abs(data.getAcceleration()/10) * 100);

        bar.setProgress(shakeValue);

        // TODO: should we give feedback in our project?
        /*if (shakeValue < 30) {    // Warning treshold.
            progress_shake.setBackgroundColor(Color.GREEN);
        } else {
            progress_shake.setBackgroundColor(Color.RED);
            if (this.fragmentView.isShown()) shakeWarning.show();
        }*/
    }

    /*
     * Visually presents the orientation of the device
     */
    private void handleOrientionVectorObserverChanged(RotationVectorObserver.DomainData data) {
        ProgressBar bar;
        int val;

        // azimuth
        bar = (ProgressBar)fragmentView.findViewById(R.id.azimuthBar);
        val = (int) ((data.getAzimuth() + Math.PI) / Math.PI / 2 * 100);
        bar.setProgress(val);

        // pitch
        bar = (ProgressBar)fragmentView.findViewById(R.id.pitchBar);
        val = (int) (Math.abs(data.getPitch()) / Math.PI * 200);
        bar.setProgress(val);
        // TODO: should we give feedback in our project?
        //bar.setBackgroundColor(val < 10 ? Color.GREEN : Color.RED);
        /*if (val >= 10) {
            if (this.fragmentView.isShown()) orientationWarning.show();
        }*/

        // roll
        bar = (ProgressBar)fragmentView.findViewById(R.id.rollBar);
        val = (int) (Math.abs(data.getRoll()) / Math.PI * 100);
        bar.setProgress(val);
        // TODO: should we give feedback in our project?
        //bar.setBackgroundColor(val < 10 ? Color.GREEN : Color.RED);
        /*if (val >= 10) {
            if (this.fragmentView.isShown()) orientationWarning.show();
        }*/
    }

    /**
     * This method handles speed events and updates the progress bar to show if speed is to high.
     */
    private void handleSpeedEvent(LocationObserver.DomainData data) {
        ProgressBar bar = (ProgressBar) fragmentView.findViewById(R.id.speedBar);
        int speedValue = (int) (data.getSpeed() * 100);

        bar.setProgress(speedValue > 0 ? speedValue : 0);

        // TODO: should we give feedback in our project?
        /*if(speedValue < 15) {
            bar.setBackgroundColor(Color.GREEN);
        } else {
            bar.setBackgroundColor(Color.RED);
            if (this.fragmentView.isShown()) speedWarning.show();
        }*/
    }
}
