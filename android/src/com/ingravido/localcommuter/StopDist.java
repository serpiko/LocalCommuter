package com.ingravido.localcommuter;

public class StopDist extends Stop {
	
	protected double dist;

	
	public StopDist(int id, String name, double lat, double lng, double dist) {
		super(id,name, lat, lng);
		
		this.dist = dist;
	}


	public double getDist() {
		return dist;
	}


	public void setDist(double dist) {
		this.dist = dist;
	}

}
