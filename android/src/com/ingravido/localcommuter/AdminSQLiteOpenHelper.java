package com.ingravido.localcommuter;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LINEA = "numlinea";
	public static final String COLUMN_PARADA = "numparada";
	public static final String COLUMN_DATE = "fecha";
	public static final String COLUMN_GPX = "gpx_user";
	public static final String COLUMN_GPY = "gpy_user";
	

	
	public static final String TABLE_LINES = "Lineas";
	public static final String TABLE_LINEFREQ = "LineaTipoFrec";
	public static final String TABLE_ROUTELINE = "RutaLinea";
	public static final String TABLE_STOP = "Parada";
	public static final String TABLE_REALTIME = "RealTimeTable";

	private static final String LINES_CREATE = "CREATE TABLE \"Lineas\" ( \"_id\" INTEGER PRIMARY KEY, \"name\" TEXT);";
	private static final String LINEFREQ_CREATE = "CREATE TABLE \"LineaTipoFrec\" ( \"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, \"numlinea\" INT NOT NULL, "
			+ "\"tipofrec\" TEXT, \"frec\" INTEGER);";
	private static final String LINEROUTE_CREATE = "CREATE TABLE \"RutaLinea\" ( \"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, \"numlinea\" INTEGER NOT NULL, "
			+ "\"numparada\" INTEGER NOT NULL,\"orden\" INT NOT NULL, \"sentido\" INT NOT NULL);";
	private static final String STOPS_CREATE = "CREATE TABLE \"Parada\" ( \"_id\" INTEGER PRIMARY KEY, \"name\" TEXT, \"lat\" REAL, \"long\" REAL);";
	private static final String REALTIME_CREATE = "CREATE TABLE \"RealTimeTable\"( \"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, \"numlinea\" INTEGER NOT NULL, \"numparada\" INTEGER NOT NULL,"
			+ "\"fecha\" TIMESTAMP NOT NULL DEFAULT current_timestamp, \"gpx_user\" REAL, \"gpy_user\" REAL );";
	protected static final String DATABASE_NAME = "localcommuter.db";
	private static final int DATABASE_VERSION = 1;

    // La ruta de sistema por defecto de las db en android data/data y /databases no cambian
    private static String DB_PATH = "/data/data/com.ingravido.localcommuter/databases/";
 
	private static SQLiteDatabase mDataBase;

	private static AdminSQLiteOpenHelper sInstance = null;
	Context context;
	
	/**
     * El Constructor crea y mantiene una referencia al contexto pasado por parámetro
     * para poder acceder a los recursos de la aplicación
     */
	private AdminSQLiteOpenHelper(Context context) {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	     try {
	    	 	this.context = context;
	            createDataBase();
	            openDataBase();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	 
	    }
	 
	    private AdminSQLiteOpenHelper(Context context, String name,
			CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	private AdminSQLiteOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

		/**
	     * Singleton para la db
	     */
	    public static AdminSQLiteOpenHelper instance(Context context) {
	    	// Uso del contexto de aplicación para asegurarnos de que no se pierde el contexto de la actividad
	        if (sInstance == null) {
	            sInstance = new AdminSQLiteOpenHelper(context);
	        }
	        return sInstance;
	    }
	 
	 
	    /**
	     * Crea una db vacía en el sistema y la reescribe con la de assets
	     */
	    private void createDataBase() throws IOException {
	 
	        boolean dbExist = checkDataBase();
	 
	        if (dbExist) {
	            // nada
	        } else {
	        	// Creamos una db vacía en la ruta x defecto para popularla con nuestros datos
	            this.getReadableDatabase();
	 
	            try {
	 
	                copyDataBase();
	 
	            } catch (IOException e) {
	 
	                throw new Error("Error copiando la db");
	            }
	        }
	    }
	 
	    /**
	     * Comprobamos si ya existe la db para no tener que copiarla cada vez que se abra la app
	     *
	     * @return true si existe, false si no
	     */
	    private boolean checkDataBase() {
	 
	        SQLiteDatabase checkDB = null;
	 
	        try {
	            String myPath = DB_PATH + DATABASE_NAME;
	            checkDB = SQLiteDatabase.openDatabase(myPath, null,
	                    SQLiteDatabase.OPEN_READONLY);
	 
	        } catch (SQLiteException e) {
	 
	            // database doesn't exist yet.
	 
	        }
	 
	        if (checkDB != null) {
	 
	            checkDB.close();
	 
	        }
	 
	        return checkDB != null;
	    }
	 
	    /**
	     * Copiamos la db original en assets a la db vacía recien creada en la carpeta
	     * de sistema, desde donde puede ser accedida y manipulada.
	     */
	    public void copyDataBase() throws IOException {
	 
	        // Se abre la db local como input stream
	    	InputStream miInput = context.getAssets().open(DATABASE_NAME);
	 
	        // Ruta de la nueva db vacía
	        String outFileName = DB_PATH + DATABASE_NAME;
	 
	        // Se abre la db vacía como output stream
	        OutputStream miOutput = new FileOutputStream(outFileName);
	        
	        //Copia de datos:
	        // transferimos los bytes del input al output
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = miInput.read(buffer)) > 0) {
	            miOutput.write(buffer, 0, length);
	        }
	 
	        //Cerramos los streams
	        miOutput.flush();
	        miOutput.close();
	        miInput.close();
	 
	    }
	 
	    private void openDataBase() throws SQLException {
	 
	        // abre la db
	        String myPath = DB_PATH + DATABASE_NAME;
	        mDataBase = SQLiteDatabase.openDatabase(myPath, null,
	                SQLiteDatabase.OPEN_READWRITE);
	    }
	 
	    /**
	     * Select method
	     *
	     * @param query select query
	     * @return - Cursor with the results
	     * @throws android.database.SQLException sql exception
	     */
	    public Cursor rawSelect(String query) throws SQLException {
	        return mDataBase.rawQuery(query, null);
	    }
	    
	    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,String having,String orderBy){
	    		return mDataBase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	    }
	 
	    /**
	     * Insert method
	     *
	     * @param table  - name of the table
	     * @param values values to insert
	     * @throws android.database.SQLException sql exception
	     */
	    public long insert(String table, ContentValues values) throws SQLException {
	        return mDataBase.insert(table, null, values);
	    }
	 
	    /**
	     * Delete method
	     *
	     * @param table - table name
	     * @param where WHERE clause, if pass null, all the rows will be deleted
	     * @throws android.database.SQLException sql exception
	     */
	    public void delete(String table, String where) throws SQLException {
	 
	        mDataBase.delete(table, where, null);
	 
	    }
	 
	    /**
	     * Update method
	     *
	     * @param table  - table name
	     * @param values - values to update
	     * @param where  - WHERE clause, if pass null, all rows will be updated
	     */
	    public void update(String table, ContentValues values, String where ) {
	 
	        mDataBase.update(table, values, where, null);
	 
	    }
	 
	    /**
	     * Let you make a raw query
	     *
	     * @param command - the sql comand you want to run
	     */
	    public void sqlCommand(String command) {
	        mDataBase.execSQL(command);
	    
	    }

		public void execSQL(String sql) {
			// TODO Auto-generated method stub
			mDataBase.execSQL(sql);
			
		}
	 
	    @Override
	    public synchronized void close() {
	 
	        if (mDataBase != null)
	            mDataBase.close();
	 
	        super.close();
	 
	    }	 
	

	@Override
	public void onCreate(SQLiteDatabase db) {}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}
