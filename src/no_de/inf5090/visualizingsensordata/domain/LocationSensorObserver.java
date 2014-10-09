package no_de.inf5090.visualizingsensordata.domain;

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
public class LocationSensorObserver extends LogicalSensorObservable implements LocationListener {
    public final static String TAG = "LocationSensorObserver";

    /**
     * Minimum distance to move for updates from sensor. Meters
     */
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // (from GPSTracker: 1, and type long, from SpeedSensor: 0.5f)

    /**
     * Minimum time between location updates from sensor. Milliseconds
     */
    private static final long MIN_TIME_BW_UPDATES = 5000; // (from GPSTracker: 1000 * 1 * 1, from SpeedSensor: 100)

    /**
     * The location manager
     */
    protected LocationManager mLocationManager;

    /**
     * Keep track if the location provider is running
     */
    private boolean mIsActive = false;

    /**
     * Last known location
     */
    private Location mLocation;

    /**
     * The unique ID for this sensor observer
     */
    public final static int ID = 103;

    /**
     * Context
     */
    private final Context mContext;

    /**
     * Constructor gets an location manager
     */
    public LocationSensorObserver(Context context){
        this.mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Searches for the location provider, which fits best to the requirements
     * @return name of the best provider
     */
    public String getBestProvider(){
        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(true);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        Log.d(TAG, "got best provider: "+bestProvider);
        return bestProvider;
    }

    /**
     * Gets called when the location is updated and notifies observers
     */
    public void onLocationChanged(Location location) {
        VideoCapture.getSelf().locationUpdate(this);
        Log.d(TAG, "onLocationChanged");

        mLocation = location;

        setChanged();
        notifyObservers(new LogicalSensorData(this));
    }

    public void onProviderDisabled(String provider) {
        Log.d(TAG, "provider disabled");
    }

    public void onProviderEnabled(String provider) {
        Log.d(TAG, "provider enabled");
    }

    /**
     * Status change from provider
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusName = "UNKNOWN";
        switch (status) {
            case LocationProvider.AVAILABLE:
                statusName = "AVAILABLE";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                statusName = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statusName = "TEMPORARILY_UNAVAILABLE";
        }

        Log.d(TAG, "status changed; status: "+statusName);
    }

    /**
     * Get last known location object
     */
    public Location getLocation() {
        return mLocation;
    }

    public double getSpeed() {
        return mLocation.getSpeed();
    }

    @Override
    public void onPause() {
        if(mLocationManager != null && mIsActive){
            mLocationManager.removeUpdates(this);
            mIsActive = false;
        }
    }

    @Override
    public void onResume() {
        if(!mIsActive) {
            mLocationManager.requestLocationUpdates(getBestProvider(), MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            mIsActive = true;
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
        private Location mLocation;
        private double mSpeed;

        public LogicalSensorData(LocationSensorObserver sensor) {
            super(sensor);
            mSpeed = sensor.getSpeed();
            mLocation = sensor.getLocation();

            Log.d(TAG, String.format("altitude: %f   longitude: %f   latitude: %f   speed: %f   speed other: %f   accuracy: %f   provider: %s", getAltitude(),
                    getLongitude(), getLatitude(), getSpeed(), mLocation.getSpeed(), mLocation.getAccuracy(), mLocation.getProvider()));
        }

        @Override
        public Element getXml() {
            Document doc = Utils.getDocumentInstance();
            Element item = getBaseXml(), elm;

            // actual sensor data
            elm = doc.createElement("Entry");
            elm.setAttribute("type", "speed");
            elm.appendChild(doc.createTextNode(Double.toString(getSpeed())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "altritude");
            elm.appendChild(doc.createTextNode(Double.toString(getAltitude())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "longitude");
            elm.appendChild(doc.createTextNode(Double.toString(getLongitude())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "latitude");
            elm.appendChild(doc.createTextNode(Double.toString(getLatitude())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "accuracy");
            elm.appendChild(doc.createTextNode(Double.toString(mLocation.getAccuracy())));
            item.appendChild(elm);

            elm = doc.createElement("Entry");
            elm.setAttribute("type", "provider");
            elm.appendChild(doc.createTextNode(mLocation.getProvider()));
            item.appendChild(elm);

            return item;
        }

        /**
         * Returns the current value
         */
        public double getSpeed() {
            return mSpeed;
        }

        /**
         * Function to get latitude
         */
        public double getLatitude() {
            return mLocation.getLatitude();
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            return mLocation.getLongitude();
        }

        /**
         * Function to get altitude
         */
        public double getAltitude() {
            return mLocation.getAltitude();
        }

        @Override
        public int getSensorID() {
            return ID;
        }

        @Override
        public String getSensorName() {
            return "Location";
        }
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
