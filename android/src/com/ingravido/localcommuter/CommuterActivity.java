package com.ingravido.localcommuter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CommuterActivity extends FragmentActivity implements
		LocationListener /* , OnQueryTextListener */{

	private Context context;
	private AdminSQLiteOpenHelper db;

	// diccionario clave valor para mantener una referencia
	// entre las posiciones del listview de líneas y el cursor
	private Hashtable<Integer, String> lineas;

	protected TextView display;
	private ListView listView;
	private ListView listView2;
	private SearchView mSearchView;
	private static final LatLng PALMA = new LatLng(39.578, 2.6446);

	protected GoogleMap mapa;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// private String provider;
	private ArrayList<Marker> markers;
	protected ArrayList<Stop> paradas;
	protected ArrayList<StopDist> aDistParadas;
	Stop paradactual;
	// posicion del usuario
	protected LatLng mipos;
	private int tempLinea;
	private RealTimetableDS datasource;
	private boolean esida;

	private LocationManager locationManager;
	private double latitude;
	private double longitude;
	private int numberOfUpdates;
	private String locationProvider;
	private Location location;

	// sentido de la ruta actual
	private int sentido;

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
		//comprobamos si está activado el gps sino se lanza el intent para activarlo
		GPSManager gps = new GPSManager(this);
		gps.start();
		// se obtiene el intent de búsqueda en caso de que se haya invocado por
		// el usuario,
		// se comprueba la acción y obtiene el string de input de usuario
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			buscaParadas(query);
		}

		display = (TextView) findViewById(R.id.display);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// definición del criterio de selección de location, por defecto
		Criteria criteria = new Criteria();
		criteria.setAccuracy(criteria.ACCURACY_FINE);
		locationProvider = locationManager.getBestProvider(criteria, true);
		location = locationManager.getLastKnownLocation(locationProvider);

		if (location != null) {
			System.out
					.println("Provider " + locationProvider + " seleccionado");
			onLocationChanged(location);
		} else {
			display.setText("Localización no disponible en este momento");
		}

		// creamos y abrimos la instancia de base de datos
		context = getApplicationContext();
		db = AdminSQLiteOpenHelper.instance(context);

		inicializaIU();
		// inicializamos lista de paradas
		paradas = new ArrayList<Stop>();
		// inicializamos el datasource de tiempos
		datasource = new RealTimetableDS(context);
		// TextView mtextSearch = Menu

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
					pintaUsuario();
				}
			}
		});
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
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
//		this.onCentrarClicked(null);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			buscaParadas(query);
		}
	}

	private void buscaParadas(String query) {
		Toast.makeText(this, "busca paradas: " + query, Toast.LENGTH_LONG)
				.show();
		String sqlquery = "select distinct l._id, l.name, p.name , p.lat, p.long, rl.orden,  rl.numparada from lineas as l, parada as p, RutaLinea as rl where ( l._id = rl.numlinea ) and (rl.numparada = p._id ) and lower(p.name) like \"%"
				+ query + "%\"";
		paradas.clear();
		consultaYPintaParadas(sqlquery);
	}

	private void consultaYPintaParadas(String sqlquery) {
		Cursor cursor = db.rawSelect(sqlquery);
		if (cursor != null && cursor.getCount() > 0) {
			Log.i(this.getClass().getSimpleName(), "entra al cursor, posicion "
					+ cursor.getPosition());
			Stop parada;
			ShowStopsAdapter adapter = new ShowStopsAdapter(this, paradas);
			listView2.setAdapter(adapter);
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			mapa.clear();
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
						posicion).title(pname));
				// Añadimos la posición del marker en el objeto
				// LatLngBounds.builder para definir un área con que delimitar
				// el mapa
				builder.include(posicion);

				// añadimos el marker al array de markers para pintar en el mapa
				// las paradas
				markers.add(marker);
				paradas.add(parada);
			}
			LatLngBounds bounds = builder.build();
			int padding = 0; // offset from edges of the map in pixels
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
					padding);
			mapa.moveCamera(cu);

		}
		cursor.close();
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

	/* Solicita actualizaciones en el reinicio */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(locationProvider, 1, 1, this);
	}

	/**
	 * Function to show settings alert dialog
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS está deshabilitado, lo quieres activar?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
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

	public void inicializaIU() {
		listView = (ListView) findViewById(R.id.listView1);
		listView2 = (ListView) findViewById(R.id.listView2);
		display = (TextView) findViewById(R.id.display);
		mapa = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		lineas = new Hashtable<Integer, String>();
		tempLinea = 0;
		// como estado inicial definimos el recorrido de ida
		esida = true;
		markers = new ArrayList<Marker>();
		// centramos el mapa en palma
//		CameraUpdate initpos = CameraUpdateFactory.newLatLngZoom(PALMA, 17);

		// Mueve inmediatamente sin animación
//		mapa.moveCamera(initpos);
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(PALMA)      // Sets the center of the map to Mountain View
	    .zoom(12)                   // Sets the zoom
	    .bearing(0)                // Sets the orientation of the camera to east
	    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
	    .build();                   // Creates a CameraPosition from the builder
	mapa.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// MenuItem searchItem = menu.findItem(R.id.action_search);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));


		return true;
	}

	// Método para gestionar las acciones del menú de opciones del ActionBar
	public boolean onOptionsItemSelected(MenuItem item) {
		Drawable direccion_icon1 = getResources().getDrawable(
				R.drawable.av_shuffle);
		Drawable direccion_icon2 = getResources().getDrawable(
				R.drawable.av_shuffle_inv);
		switch (item.getItemId()) {

		case R.id.refreshlines:
			Toast.makeText(CommuterActivity.this, "actualiza vista",
					Toast.LENGTH_SHORT).show();
			// limpiamos el mapa de objetos marker
			mapa.clear();
			pintaUsuario();

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
			if (esida) {
				// change your view and sort it by Alphabet
				item.setIcon(direccion_icon1);
				item.setTitle("ida");
				esida = false;
			} else {
				// change your view and sort it by Date of Birth
				item.setIcon(direccion_icon2);
				item.setTitle("vuelta");
				esida = true;
			}

			this.onToggleClicked(null);
			return true;
		case R.id.menu_user:
			Toast.makeText(context, "centrar mapa en usuario",
					Toast.LENGTH_SHORT).show();
			// Centramos el mapa en la posición del usuario
			this.onCentrarClicked(null);
			return true;
		case R.id.menu_new:
			Toast.makeText(context, "guardar", Toast.LENGTH_SHORT).show();
			this.onCargarClicked(null);
			return true;
		case R.id.timetables:
			this.onNextBusClicked(null);
			return true;
		case R.id.closestops:
			this.ParadasCerca();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

		// }
		// return false;

	}

	private void ParadasCerca() {
		
	}

	private void pintaUsuario() {
		mapa.addMarker(new MarkerOptions()
				.position(mipos)
				.title("yo")
				.icon(BitmapDescriptorFactory
						.fromResource(android.R.drawable.sym_def_app_icon)));

	}

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
		// int sentido;
		String desc = "";
		if (esida == true) {
			desc = "DESC";
			sentido = 0;
		} else {
			sentido = 1;
		}

		String query = "select l._id, l.name, p.name, p.lat, p.long, rl.orden,  rl.numparada from lineas as l, parada as p, RutaLinea as rl where ( l._id = rl.numlinea ) and (rl.numparada = p._id ) and l._id = "
				+ String.valueOf(tempLinea)
				+ " and rl.sentido = "
				+ sentido
				+ " order by rl.orden " + desc;
		consultaYPintaParadas(query);
	}

	public void onCentrarClicked(View view) {

		try {
			CameraUpdate centro = CameraUpdateFactory.newLatLngZoom(mipos, 15);
			mapa.moveCamera(centro);
			pintaUsuario();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updatePos(LatLng newpos) {
		// this.mipos.latitude = newpos.latitude;
		// this.mipos.longitude = newpos.longitude;
		this.mipos = newpos;
	}

	/* Quita las location updates cuando se pausa la app */
	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		display.setText("latitud =" + lat + " longitud =" + lng);
		updatePos(new LatLng(lat, lng));

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "deshabilitado el proveedor: " + provider,
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "habilitado un nuevo proveedor: " + provider,
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	public void onNextBusClicked(View view) {
		TextView display = (TextView) findViewById(R.id.display);

		// Log.i(this.getClass().getSimpleName(), "Entra en next bus clicked");
		// Validación de los campos línea y parada
		if (paradactual == null) {
			Toast.makeText(context, "seleccione línea y parada",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String sql = "select numlinea, numparada, time(fecha) as tiempo from realtimetable where numlinea="
				+ tempLinea
				+ " and numparada = "
				+ paradactual.getId()
				+ " and tiempo > time(current_timestamp, 'localtime') limit 3;";
		Log.i(this.getClass().getName(), sql);
		String list = new String("vacio");

		Cursor cursor = db.rawSelect(sql);
		if (cursor != null && cursor.getCount() > 0) {
			Log.i(this.getClass().getSimpleName(), "entra al cursor, posicion "
					+ cursor.getPosition());
			cursor.moveToFirst();
			display.setText("");
			while (cursor.moveToNext()) {

				int linea = cursor.getInt(0);
				int parada = cursor.getInt(1);
				String hora = cursor.getString(2);
				Log.i(this.getClass().getCanonicalName(), "next bus linea"
						+ linea + " parada" + parada + " hora " + hora);
				// lineas.put(Integer.valueOf(id), s);
				// list += "Siguiente bus de Linea " + String.valueOf(linea)
				// + " Parada " + String.valueOf(parada)
				// + " Hora de llegada " + hora + "\n";
				list = "Siguiente bus de Linea " + Integer.toString(linea)
						+ " Parada " + Integer.toString(parada)
						+ " Hora de llegada " + hora + "\n";

			}
			Toast.makeText(context, list, Toast.LENGTH_SHORT).show();
			display.setText(list);
		}
		// + paradactual.id + "a las " + dateFormat.format(cal.getTime()));
		// }
	}

	public void onCargarClicked(View view) {
		// Pedimos al usuario que seleccione linea y parada si no lo ha hecho
		// antes
		// datasource.open();
		// List<RealTimetable> horarios = datasource.getAllRTT();
		// ListView l1 = (ListView) findViewById(R.id.listView1);
		// ArrayAdapter<RealTimetable> adapter = new
		// ArrayAdapter<RealTimetable>(
		// context, android.R.layout.simple_list_item_1, horarios);
		// listView.setAdapter(adapter);
		/* Alert Dialog Code Start */
		// TODO arreglar comprobarSeleccion y borrar siguiente if
		// comprobarSeleccion();
		if (paradactual == null) {
			Toast.makeText(context, "seleccione línea y parada",
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("añadir nuevo horario observado"); // Set Alert dialog
		// title
		// // here
		alert.setMessage("linea" + tempLinea + "parada:"
				+ paradactual.getName() + "?"); // Message here

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				TextView display = (TextView) findViewById(R.id.display);

				String sql = "INSERT INTO realtimetable ( numlinea, numparada, fecha, gpx_user, gpy_user) VALUES ("
						+ tempLinea
						+ ", "
						+ paradactual.getId()
						+ ", "
						+ "time(current_timestamp, 'localtime'),"
						+ mipos.latitude + ", " + mipos.longitude + ");";

				db.execSQL(sql);
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				System.out.println(dateFormat.format(cal.getTime()));
				Toast.makeText(context, "nuevo horario observado incluido",
						Toast.LENGTH_SHORT);

				display.setText("llegando bus de la linea" + tempLinea
						+ " en parada " + paradactual.id + "a las "
						+ dateFormat.format(cal.getTime()));

			}
		});

		alert.setNegativeButton("CANCEL",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
		/* Alert Dialog Code End */

	}

	private void comprobarSeleccion() {
		if (paradactual == null) {
			Toast.makeText(context, "seleccione línea y parada",
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	// Pintamos en el mapa y anotamos en los listviews las paradas más cercanas
	public void onCloseStopsClicked(View view) {
		//
		// ##13-06-2013
		// Funcionalidad para buscar las paradas más cercanas.
		// -cargar las coordenadas a partir de la tabla parada en un arraylist
		// -recorrer el array y comparar con la función
		// float distance = locationA.distanceTo(locationB);
		// -siendo locationA:
		// locationA.setLatitude(mipos.latitude);
		// locationB.setLongitude(mipos.longitude);

		final String query = "select _id, name, lat, long from parada;";
		Cursor cursor = db.rawSelect(query);
		if (cursor != null && cursor.getCount() > 0) {
			Log.i(this.getClass().getCanonicalName(),
					"Posición cursor Distancias" + cursor.getPosition());
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			// Eliminamos ' y " de las coordenadas
			String plat = cursor.getString(3).replaceAll("\'|\"", "");
			String plong = cursor.getString(3).replaceAll("\'|\"", "");

			Double llat = Double.parseDouble(plat);
			Double llong = Double.parseDouble(plong);

		}

	}

	public void onToggleClicked(View view) {
		paradas.clear();
		updateListView1(tempLinea);
	}

	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}
}
