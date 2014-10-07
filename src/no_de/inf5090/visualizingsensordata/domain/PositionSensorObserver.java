package no_de.inf5090.visualizingsensordata.domain;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jens Naber et al.
 * Analyzes the GPS data to find out if the user is walking to fast to film a good video
 */
public class PositionSensorObserver extends LogicalSensorObservable implements LocationListener{
    // The minimum distance to change Updates in meters
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.5f; // meters (from GPSTracker: 1, and type long)

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 100; // milliseconds (from GPSTracker: 1000 * 1 * 1)

	private static final int ACCURACY_THRESHOLD = 30; // meter
	private static final int ACCURACY_ERROR = 2; // count
	private static final int AVERAGE_VALUE_RANGE = 4; //count	
	private static final float LOWER_THRESHOLD = 0.8f; //meter/seconds
	private static final float UPPER_THRESHOLD = 1.7f; //meter/seconds	

	protected LocationManager locationManager;
	int accuracyErrorCount = ACCURACY_ERROR;
	LinkedList<Float> speedQueue = new LinkedList<Float>();
	boolean lastSpeedZero = false;
	boolean usesGPS = false;
	double normalizedSpeed = -2;

    /**
     * The unique ID for this sensor observer
     */
    public final static int ID = 103;
	
	private final Context mContext;

	/**
	 * Constructor gets an location manager
	 */
	public PositionSensorObserver(Context context){
        this.mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Searches for the location provider, which fits best to the requirements
	 * @return name of the best provider
	 */
	public String getBestProvider(){
	    Criteria criteria = new Criteria();
	    criteria.setSpeedRequired(true);
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    String bestProvider = locationManager.getBestProvider(criteria, true);
	    return bestProvider;
	}

	/** 
	 * Gets called when the location is updated. Smoothes the speed data by calculating the moving average. 
	 * Will check for the GPS accuracy. Speed data is only used if the signal is accurate enough, returns error value else.
	 * Normalizes the speed to the range of 0-1.
	 * 
	 * Notifies the observers about the result value. Sends -2 if no speed data is provided or the signal was not accurate enough.
	 * 
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 * @param location GPS location 
	 */
	public void onLocationChanged(Location location) {
        VideoCapture.getSelf().locationUpdate(this);
        Log.d("GPS", "onLocationChanged");

        normalizedSpeed = -2;
		
		if(location.hasSpeed()) {
			// checks if the accuracy is low for the second time, if so return -2 and empty queue
			if(location.getAccuracy() > ACCURACY_THRESHOLD) {
				if(accuracyErrorCount < ACCURACY_ERROR) {
					accuracyErrorCount++;
				}
				else {
					normalizedSpeed = -2;
					speedQueue.clear();
				}
			}
			else {
				accuracyErrorCount = 0;
			}
			
			// move on if accuracy is ok
			if(accuracyErrorCount < ACCURACY_ERROR) {
				normalizedSpeed = 0;
				
				// if the speed is zero for the second time, assume user has stopped. return 0 and empty queue
				if(lastSpeedZero && location.getSpeed() == 0) {
					speedQueue.clear();
				}
				else {
					// remember always the last AVERAGE_VALUE_RANGE speed values and calculate average.
					if(speedQueue.size() < AVERAGE_VALUE_RANGE) {
						speedQueue.add(location.getSpeed());
					}
					else {
						speedQueue.remove();
						speedQueue.add(location.getSpeed());
					}
					
					for(int x=0; x<speedQueue.size(); x++)
						normalizedSpeed += speedQueue.get(x);
					
					normalizedSpeed /= speedQueue.size();
					lastSpeedZero = location.getSpeed() == 0;
				}
			}
		}
		
		// normalize the data
		if(normalizedSpeed != -2) {
			normalizedSpeed = (normalizedSpeed-LOWER_THRESHOLD)/(UPPER_THRESHOLD-LOWER_THRESHOLD);
			normalizedSpeed = normalizedSpeed<0?0:normalizedSpeed;
			normalizedSpeed = normalizedSpeed>1?1:normalizedSpeed;
		}
		
		setChanged();
		notifyObservers(new LogicalSensorData(this));
	}

	public void onProviderDisabled(String provider) {
	
	}


	public void onProviderEnabled(String provider) {
	
	}

	/**
	 * Sends -2 to the observers, if the GPS Sensor isn't available anymore.
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(status != LocationProvider.AVAILABLE) {
			setChanged();
			notifyObservers(-2.0);
		}
	}
	
	public double getSpeed() {
		return normalizedSpeed;
	}

    @Override
    public void onPause() {
        if(locationManager != null && usesGPS){
            locationManager.removeUpdates(this);
            usesGPS = false;
        }
    }

    @Override
    public void onResume() {
        if(!usesGPS) {
            locationManager.requestLocationUpdates(getBestProvider(), MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            usesGPS = true;
        }
    }

    @Override
    public int getSensorID() {
        return ID;
    }

    /**
     * Sensor observer data
     */
    public class LogicalSensorData extends AbstractLogicalSensorData {
        private double speed;

        public LogicalSensorData(PositionSensorObserver sensor) {
            super(sensor);
            this.speed = sensor.getSpeed();
        }

        @Override
        public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml(), elm;

            // actual sensor data
            elm = doc.createElement("Speed");
            elm.appendChild(doc.createTextNode(Double.toString(getSpeed())));
            item.appendChild(elm);

            return item;
        }

        /**
         * Returns the current value
         */
        public double getSpeed() {
            return speed;
        }

        @Override
        public int getSensorID() {
            return ID;
        }

        @Override
        public String getSensorName() {
            return "Speed";
        }
    }

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }


    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     * */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
}
