package no_de.inf5090.visualizingsensordata.application;

import java.util.ArrayList;

import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean gpsActive = false;

	boolean gpsFixed = false;
	
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager locationManager;

	private ArrayList<Location> track;
	
	public GPSTracker(Context context) {
		this.mContext = context;
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);		
		setTrack(new ArrayList<Location>());
		startUsingGPS();
	}

	/**
	 * Searches for the location provider, which fits best to the requirements
	 * @return name of the best provider
	 */
	public String getBestProvider(){	    
	    Criteria criteria = new Criteria();
	    criteria.setSpeedRequired(false);
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    String bestProvider = locationManager.getBestProvider(criteria, true);
	    return bestProvider;
	}

	/**
	 * Resets the GPS track 
	 */
	public void startNewTrack(){
		this.track.clear(); 
		
	}
	
	/**
	 * registers as an LocationListener, if it not already happened
	 */
	public void startUsingGPS() {
		if(gpsActive == false){
			locationManager.requestLocationUpdates(getBestProvider(), MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			gpsActive = true;
		}
	}


	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null && gpsActive == true) {
			locationManager.removeUpdates(GPSTracker.this);
			gpsActive = false;
		}
	}

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

	public void onLocationChanged(Location location) {
		
		if(gpsFixed==false){
			VideoCapture.getSelf().enableButton();
			gpsFixed = true;
		}
		Log.d("GPS", "onLocationChanged");
		track.add(location);
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public ArrayList<Location> getTrack() {
		return track;
	}

	public void setTrack(ArrayList<Location> track) {
		this.track = track;
	}

}
