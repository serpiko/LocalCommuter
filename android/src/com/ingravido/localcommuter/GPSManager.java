package com.ingravido.localcommuter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GPSManager {

	private Activity activity;
	private LocationManager mlocManager;
	private LocationListener gpsListener;
	protected CommuterActivity context;

	public GPSManager(Activity activity) {
		this.activity = activity;
		this.context = (CommuterActivity) activity;
	}

	public void start() {
//		mlocManager = (LocationManager) activity
//				.getSystemService(Context.LOCATION_SERVICE);
		mlocManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Lanzamos la instancia de GPSListener que implementa
			// LocationListener
			// para gestionar actualizaciones del sensor
			gpsListener = new GPSListener(context, mlocManager);
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
					1, gpsListener);

			if (mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
				Toast.makeText(activity, "LAST Location null",
						Toast.LENGTH_SHORT).show();
			else {
				Location loc = mlocManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				gpsListener.onLocationChanged(loc);
				// Actualizamos el textView desde aquí
				String message = "Current location is:  Latitude = "
						+ loc.getLatitude() + ", Longitude = "
						+ loc.getLongitude();
				Log.i("GPSManger.start","lat "+ loc.getLatitude()+" long "+loc.getLongitude());
//				updateDisplay(message);

			}
			// setUp();
			// findLoc();
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					activity);
			alertDialogBuilder
					.setMessage("el GPS está deshabilitado, desea activarlo?")
					.setCancelable(false)
					.setPositiveButton("Habilitar GPS",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									activity.startActivity(callGPSSettingIntent);
								}
							});
			alertDialogBuilder.setNegativeButton("Cancelar",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = alertDialogBuilder.create();
			alert.show();

		}

	}

	public void setUp() {
		Log.i("GPSManager", "llamando instancia GPSListener");
		gpsListener = new GPSListener(activity, mlocManager);
	}

	public void findLoc() {
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1,
				gpsListener);

		if (mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
			Toast.makeText(activity, "LAST Location null", Toast.LENGTH_SHORT)
					.show();
		else {
			 gpsListener.onLocationChanged(mlocManager
			 .getLastKnownLocation(LocationManager.GPS_PROVIDER));
			 Location loc = mlocManager
			 .getLastKnownLocation(LocationManager.GPS_PROVIDER);
			 gpsListener.onLocationChanged(loc);
			 // Actualizamos el textView desde aquí
			 String message = "la ubicación actual es:  Latitude = "
			 + loc.getLatitude() + ", Longitude = " + loc.getLongitude();
			// updateDisplay(message);

			// //
//			Location loc = mlocManager
//					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			gpsListener.onLocationChanged(loc);
//			// Actualizamos el textView desde aquí
//			String message = "Current location is:  Latitude = "
//					+ loc.getLatitude() + ", Longitude = " + loc.getLongitude();
//			updateDisplay(message);

		}
	}

	public void updateDisplay(final String message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				context.display.setText(message);

				Log.i(this.getClass().getSimpleName(), "posición de usuario "
						+ message);
			}
		});
	}
}
