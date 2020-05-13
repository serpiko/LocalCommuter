package com.ingravido.localcommuter;

//private static final String REALTIME_CREATE = "CREATE TABLE \"HorarioReal\"( \"_id\" INTEGER PRIMARY KEY AUTOINCREMENT, \"numlinea\" INTEGER NOT NULL, \"numparada\" INTEGER NOT NULL," + 
//"\"fecha\" TEXT NOT NULL, \"gpx_user\" REAL, \"gpy_user\" REAL );";

/*se crea clase para manejar la información de horarios reales observados.*/

/*CREATE TABLE "RealTimeTable" ( "_id" INTEGER PRIMARY KEY AUTOINCREMENT, "numlinea" INTEGER NOT NULL, 
 * "numparada" iNTEGER NOT NULL, "fecha" TEXT NOT NULL, "gpx_user" REAL, "gpy_user" REAL );
 
 sqlite> insert into realtimetable (numlinea, numparada, fecha, gpx_user, gpy_user) 
 VALUES( 28, 471, datetime('2013-05-01 00:00:00'), 38.5590705871582, 2.6072471141815186 );*/

//el campo fecha lo añadimos como datetime('now')

public class RealTimetable {
	private int id;
	private int numlinea;
	private int numparada;
	private int hora;
	//fecha se añade directamente a la db como datetime('now'), en principio no definimos una variable, si lo hicieramos usar int que basta para precisión dia-hora-segundo
	private double gpx_user;
	private double gpy_user;
	
	public RealTimetable(){
		
	}
	public RealTimetable(int numlinea, int numparada, double gpx_user, double gpy_user){
		this.numlinea = numlinea;
		this.numparada = numparada;
		this.gpx_user = gpx_user;
		this.gpy_user = gpy_user;
	}
	


	public double getGpx_user() {
		return gpx_user;
	}
	public void setGpx_user(double gpx_user) {
		this.gpx_user = gpx_user;
	}
	public double getGpy_user() {
		return gpy_user;
	}
	public void setGpy_user(double gpy_user) {
		this.gpy_user = gpy_user;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the numlinea
	 */
	public int getNumlinea() {
		return numlinea;
	}
	/**
	 * @param numlinea the numlinea to set
	 */
	public void setNumlinea(int numlinea) {
		this.numlinea = numlinea;
	}
	/**
	 * @return the numparada
	 */
	public int getNumparada() {
		return numparada;
	}
	/**
	 * @param numparada the numparada to set
	 */
	public void setNumparada(int numparada) {
		this.numparada = numparada;
	}
	/**
	 * @return the hora
	 */
	public int getHora() {
		return hora;
	}
	/**
	 * @param hora the hora to set
	 */
	public void setHora(int hora) {
		this.hora = hora;
	}
}
