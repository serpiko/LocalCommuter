package com.ingravido.localcommuter;

public class Stop {
	
	
	protected int id;
	protected String name;
	protected double lat;
	protected double lng;

	public int getId() {
		return id;
	}

	public Stop(int id, String name, double lat, double lng) {
		super();
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

}
