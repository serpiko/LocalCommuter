package com.ingravido.localcommuter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CommuterActivityBackup extends FragmentActivity implements
		LocationListener {

	private Context context;
	private AdminSQLiteOpenHelper db;

	// diccionario clave valor para mantener una referencia
	// entre las posiciones del listview de líneas y el cursor
	private Hashtable<Integer, String> lineas;

	protected TextView display;
	private ListView listView;
	private ListView listView2;
	private static final LatLng PALMA = new LatLng(39.578, 2.6446);

	protected GoogleMap mapa;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private String provider;
	private ArrayList<Marker> markers;
	protected ArrayList<Stop> paradas;
	protected ArrayList<StopDist> aDistParadas;
	Stop paradactual;
	// posicion del usuario
	protected LatLng mipos;
	private int tempLinea;
	private RealTimetableDS datasource;
	private int esida = 0;
	private LocationManager locationManager;
	private boolean canGetLocation;
	private LocationListener locationListenerNetwork;
//	private Location location;
	private LocationListener locationListenerGps;
	private double latitude;
	private double longitude;
	private Location loc;
	private Location location;
	//
    private LocationManager lm;
    private int numberOfUpdates;

    public static final int MAX_NUMBER_OF_UPDATES = 10;
//
	// constantes de tablas y columnas
	public static final String TABLE_LINES = "Lineas";
	public static final String TABLE_STOPS = "Parada";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_ID = "_id";;
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LONG = "long";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Inicializamos variables de aplicación
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_commuter);
		// creamos y abrimos la instancia de base de datos
		context = getApplicationContext();
		db = AdminSQLiteOpenHelper.instance(context);
		// this.onCentrarClicked(null);
		// Comprobamos si está activado el GPS y sino lanzamos el intent para
		// que el usuario lo habilite en la configuración del dispositivo.
		// instanciamos un objeto gps del GPSManager al que le pasamos el
		// contexto de la actividad
//		 GPSManager gps = new GPSManager(CommuterActivity.this);
		// gps.start();
		// TODO: temp
		// LocationManager service = (LocationManager)
		// getSystemService(LOCATION_SERVICE);
		// boolean enabled = service
		// .isProviderEnabled(LocationManager.GPS_PROVIDER);
		// if (!enabled) {
		// Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		// startActivity(intent);
		// //
		// }

		inicializaIU();

		loc = getLocationMaganer();

		// inicializamos lista de paradas
		paradas = new ArrayList<Stop>();
		// inicializamos el datasource de tiempos
		datasource = new RealTimetableDS(context);
		// locationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		// centramos el mapa en palma
		// CameraUpdate initpos = CameraUpdateFactory.newLatLngZoom(PALMA, 17);

		// Mueve inmediatamente sin animación
		// mapa.moveCamera(initpos);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {

				// ContentValues values = new ContentValues();
				final String item = (String) arg0.getItemAtPosition(position);
				// Extraemos num linea del string al principio de la cadena
				Pattern esid = Pattern.compile("\\d*");
				Matcher match = esid.matcher(item);
				listView.getFocusables(position);

				listView.setSelected(true);

				// int duration = Toast.LENGTH_SHORT;
				if (match.find()) {
					String inmatch = match.group();

					// Toast toast = Toast.makeText(context, inmatch, duration);
					// toast.show();
					tempLinea = Integer.parseInt(inmatch);
					// Log.i(this.getClass().getSimpleName(), "tempLinea"
					// + tempLinea);
					// limpiamos la lista de paradas
					paradas.clear();
					updateListView1(tempLinea);
				}
			}
		});
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				// TODO Auto-generated method stub

				// recuperamos la información del objeto Stop vinculado con la
				// posición pulsada en el listview

				paradactual = paradas.get(position);
				LatLng pos = new LatLng(paradactual.getLat(), paradactual
						.getLng());
				CameraUpdate centro = CameraUpdateFactory
						.newLatLngZoom(pos, 19);
				mapa.moveCamera(centro);

				// Calculamos la distancia del usuario con respecto a la parada
				Location locationA = new Location("usuario");
				TextView display = (TextView) findViewById(R.id.display);
				if ((mipos.latitude > 0) && (mipos.longitude > 0)) {
					locationA.setLatitude(mipos.latitude);
					locationA.setLongitude(mipos.longitude);

					Location locationB = new Location("parada");

					locationB.setLatitude(paradactual.lat);
					locationB.setLongitude(paradactual.lng);

					float distance = locationA.distanceTo(locationB);

					display.setText("Esta parada está a"
							+ Float.toString(distance) + " metros del usuario");

				} else {
					display.setText("Posición de usuario no inicializada:"
							+ mipos.latitude + " " + mipos.longitude);

				}
			}

		});
		// this.onCentrarClicked(null);

	}

	private Location getLocationMaganer() {

		try {
			locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			boolean isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			boolean isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 0,
							locationListenerNetwork);
					// locationManager.requestLocationUpdates(
					// LocationManager.NETWORK_PROVIDER,
					// MIN_TIME_BW_UPDATES,
					// MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 0,
								locationListenerGps);
						// locationManager.requestLocationUpdates(
						// LocationManager.GPS_PROVIDER,
						// MIN_TIME_BW_UPDATES,
						// MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}
			// if (!isGPSEnabled) {
			// showSettingsAlert();
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
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
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS está deshabilitado, lo quieres activar?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }

	public void inicializaIU() {
		listView = (ListView) findViewById(R.id.listView1);
		listView2 = (ListView) findViewById(R.id.listView2);
		display = (TextView) findViewById(R.id.display);
		mapa = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		lineas = new Hashtable<Integer, String>();
		tempLinea = 0;
		markers = new ArrayList<Marker>();
		// centramos el mapa en palma
		CameraUpdate initpos = CameraUpdateFactory.newLatLngZoom(PALMA, 15);

		// Mueve inmediatamente sin animación
		mapa.moveCamera(initpos);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Método para gestionar las acciones del menú de opciones del ActionBar
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.refreshlines:
			Toast.makeText(CommuterActivityBackup.this, "actualiza vista",
					Toast.LENGTH_SHORT).show();
			// limpiamos el mapa de objetos marker
			mapa.clear();
			// pintaUsuario();

			try {
				// carga del listview1 con las líneas
				updateListView();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.menu_direccion:
			Toast.makeText(context, "cambio de dirección", Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.menu_user:
			Toast.makeText(context, "centrar mapa en usuario",
					Toast.LENGTH_SHORT);
			// Centramos el mapa en la posición del usuario
			this.onCentrarClicked(null);
		case R.id.menu_new:
			Toast.makeText(context, "guardar", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.timetables:
			Toast.makeText(context, "horarios", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

		// }
		// return false;

	}

	private void pintaUsuario() {
		mapa.addMarker(new MarkerOptions()
				.position(mipos)
				.title("yo")
				.icon(BitmapDescriptorFactory
						.fromResource(android.R.drawable.sym_def_app_icon)));

	}

	// @Override
	// public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	// return false;
	// }

	public void updateListView() throws IOException {
		Cursor cursor = null;
		final ArrayList<String> list = new ArrayList<String>();
		final ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		try {
			// File dbFile =
			// context.getDatabasePath(AdminSQLiteOpenHelper.DATABASE_NAME);
			// Log.d("DB PATH", dbFile.getAbsolutePath());
			// Log.d("DB SIZE", "tamaño"+ dbFile.length() + "");
			cursor = db.rawSelect("SELECT * FROM " + TABLE_LINES);

			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String s = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
						.replace("\\", "");
				lineas.put(Integer.valueOf(id), s);
				list.add(id + s);
			}
			cursor.close();
		} catch (SQLException e) {
			Log.e("Error accediendo a db", e.toString());

		}
	}

	public void updateListView1(int position) {

		String desc = "";
		if (esida == 1) {
			desc = "DESC";
		}

		String query = "select l._id, l.name, p.name, p.lat, p.long, rl.orden,  rl.numparada from lineas as l, parada as p, RutaLinea as rl where ( l._id = rl.numlinea ) and (rl.numparada = p._id ) and l._id = "
				+ String.valueOf(tempLinea)
				+ " and rl.sentido = "
				+ esida
				+ " order by rl.orden " + desc;

		Cursor cursor = db.rawSelect(query);
		if (cursor != null && cursor.getCount() > 0) {
			// Log.i(this.getClass().getSimpleName(),
			// "entra al cursor, posicion "
			// + cursor.getPosition());
			Stop parada;
			ShowStopsAdapter adapter = new ShowStopsAdapter(this, paradas);
			listView2.setAdapter(adapter);
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			while (cursor.moveToNext()) {

				int lid = cursor.getInt(0);
				String lname = cursor.getString(1);
				String pname = cursor.getString(2);
				String plat = cursor.getString(3).replaceAll("\'|\"", "");

				String plong = cursor.getString(4).replaceAll("\'|\"", "");

				Double llat = Double.parseDouble(plat);
				Double llong = Double.parseDouble(plong);
				int rlorden = cursor.getInt(5);
				int pid = cursor.getInt(6);
				parada = new Stop(pid, lname + " " + pname, llat, llong);

				LatLng posicion = new LatLng(llat, llong);
				Marker marker = mapa.addMarker(new MarkerOptions().position(
						posicion).title(pname)); // ...
				// Añadimos la posición del marker en el objeto
				// LatLngBounds.builder para definir un área con que delimitar
				// el mapa
				builder.include(posicion);

				// añadimos el marker al array de markers para pintar en el mapa
				// las paradas
				markers.add(marker);
				// printMarkers();
				// pintaParada(pname, posicion);

				paradas.add(parada);
				// list.add(pname + " " + plat + " " + plong);
			}
			// LatLngBounds.Builder builder = new LatLngBounds.Builder();
			// for (Marker m : markers) {
			// builder.include(m.getPosition());
			// }
			LatLngBounds bounds = builder.build();
			int padding = 0; // offset from edges of the map in pixels
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
					padding);
			mapa.moveCamera(cu);
		}
		cursor.close();

	}

	public void onCentrarClicked(View view) {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		Location location = service.getLastKnownLocation(provider);
		mipos = new LatLng(location.getLatitude(), location.getLongitude());
		CameraUpdate centro = CameraUpdateFactory.newLatLngZoom(mipos, 15);

		// Mueve inmediatamente sin animación
		mapa.moveCamera(centro);
		try {
			pintaUsuario();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// try{
		// mapa.addMarker(new MarkerOptions()
		// .position(mipos)
		// .title("yo")
		// .icon(BitmapDescriptorFactory
		// .fromResource(android.R.drawable.sym_def_app_icon)));
		// Animate camera mueve la camara en secuencia
		// mapa.animateCamera(zoom);
	}

	protected void updatePos(LatLng newpos) {
		// this.mipos.latitude = newpos.latitude;
		// this.mipos.longitude = newpos.longitude;
		this.mipos = newpos;
	}

	//
	// @Override
	// public void onConnectionFailed(ConnectionResult connectionResult) {
	// /*
	// * Google Play services can resolve some errors it detects. If the error
	// * has a resolution, try sending an Intent to start a Google Play
	// * services activity that can resolve error.
	// */
	// if (connectionResult.hasResolution()) {
	// try {
	// // Start an Activity that tries to resolve the error
	// connectionResult.startResolutionForResult(this,
	// CONNECTION_FAILURE_RESOLUTION_REQUEST);
	// /*
	// * Thrown if Google Play services canceled the original
	// * PendingIntent
	// */
	// } catch (IntentSender.SendIntentException e) {
	// // Log the error
	// e.printStackTrace();
	// }
	// } else {
	// /*
	// * If no resolution is available, display a dialog to the user with
	// * the error.
	// */
	// showDialog(connectionResult.getErrorCode());
	// }
	//
	// }

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */

	// @Override
	// public void onConnected() {
	// // TODO Auto-generated method stub
	// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
	//
	// }
	//
	// @Override
	// public void onDisconnected() {
	// // Display the connection status
	// Toast.makeText(this, "Disconnected. Please re-connect.",
	// Toast.LENGTH_SHORT).show();
	//
	// }

	@Override
	public void onLocationChanged(Location location) {
		 if (numberOfUpdates < MAX_NUMBER_OF_UPDATES) {
	            numberOfUpdates++;

	            Log.w("LAT", String.valueOf(loc.getLatitude()));
	            Log.w("LONG", String.valueOf(loc.getLongitude()));
	            Log.w("ACCURACY", String.valueOf(loc.getAccuracy() + " m"));
	            Log.w("PROVIDER", String.valueOf(loc.getProvider()));
	            Log.w("SPEED", String.valueOf(loc.getSpeed() + " m/s"));
	            Log.w("ALTITUDE", String.valueOf(loc.getAltitude()));
	            Log.w("BEARING", String.valueOf(loc.getBearing() + " degrees east of true north"));

	            String message;

	            if (loc != null) {
	                message = "Current location is:  Latitude = "
	                        + loc.getLatitude() + ", Longitude = "
	                        + loc.getLongitude();
	                 lm.removeUpdates(this);
	            } else
	                message = "Location null";

//	            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
//	            MainActivity.mipos = new LatLng(loc.getLatitude(), loc.getLongitude());
//	            LatLng newpos = new LatLng(loc.getLatitude(), loc.getLongitude());
//	            activity.updatePos(newpos); 
	            
	        } else {
	            lm.removeUpdates(this);
	        }

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Gps Disabled", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Gps Enabled", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
