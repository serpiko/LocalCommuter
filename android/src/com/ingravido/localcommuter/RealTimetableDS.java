package com.ingravido.localcommuter;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//This class is our DAO. It maintains the database connection and supports fetching all timetable recovered from user input
public class RealTimetableDS {
	private SQLiteDatabase db;
	private AdminSQLiteOpenHelper dbHelper;
	private String[] allColumns = { /* AdminSQLiteOpenHelper.COLUMN_ID, */
			AdminSQLiteOpenHelper.COLUMN_LINEA,
			AdminSQLiteOpenHelper.COLUMN_PARADA,
			AdminSQLiteOpenHelper.COLUMN_DATE,
			AdminSQLiteOpenHelper.COLUMN_GPX, AdminSQLiteOpenHelper.COLUMN_GPY };

	public RealTimetableDS(Context context) {
		dbHelper = AdminSQLiteOpenHelper.instance(context);
	}

	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/*
	 * private ; private ; private ;
	 */
	/*
	 * Cursor cursor = database.rawQuery( "SELECT item_id AS _id," +
	 * " (strftime('%s', added_on, 'unixepoch') * 1000) AS added_on," +
	 * " added_by, quantity, units" + " FROM curr
	 */
	public RealTimetable createRealTimetable(int numlinea, int numparada, double gpx, double gpy){
//	RealTimetable r = new RealTimetable();
	ContentValues values = new ContentValues();
	values.put(AdminSQLiteOpenHelper.COLUMN_LINEA, numlinea);
	values.put(AdminSQLiteOpenHelper.COLUMN_PARADA, numparada);
	values.put(AdminSQLiteOpenHelper.COLUMN_DATE, "datetime('now')");
	values.put(AdminSQLiteOpenHelper.COLUMN_GPX, 33.555);
	values.put(AdminSQLiteOpenHelper.COLUMN_GPY, 2.999);
	long insertId = db.insert(AdminSQLiteOpenHelper.TABLE_REALTIME, null, values);
	
	Cursor cursor = db.query(AdminSQLiteOpenHelper.TABLE_REALTIME, allColumns, AdminSQLiteOpenHelper.COLUMN_ID + "=" + insertId, null, null, null, null);
//	Cursor cursor = dbHelper.rawSelect(query);
//			if (cursor != null && cursor.getCount() > 0) {
//				Log.i(this.getClass().getSimpleName(), "entra al cursor, posicion "
//						+ cursor.getPosition());
//				cursor.moveToFirst();
	cursor.moveToFirst();
	RealTimetable new_rtt = cursorToRealTimetable(cursor);
	cursor.close();
	return new_rtt;
	

	}

	public List<RealTimetable> getAllRTT() {
		List<RealTimetable> rtt_list = new ArrayList<RealTimetable>();
		Cursor cursor = db.query(AdminSQLiteOpenHelper.TABLE_REALTIME,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RealTimetable rtt = cursorToRealTimetable(cursor);
			rtt_list.add(rtt);
			cursor.moveToNext();
		}
		cursor.close();
		return rtt_list;
	}

	private RealTimetable cursorToRealTimetable(Cursor cursor) {
		RealTimetable rtt = new RealTimetable();
		rtt.setId(cursor.getInt(0));
		rtt.setNumlinea(cursor.getInt(1));
		rtt.setNumparada(cursor.getInt(2));
		rtt.setHora(cursor.getInt(3));
		rtt.setGpx_user(cursor.getLong(4));
		rtt.setGpy_user(cursor.getLong(5));
		return rtt;
	}

}
