package caceresenzo.server.drone.api.flight.models;

import java.util.Date;

import caceresenzo.libs.json.JsonObject;

public class FlightPoint {
	
	/* Variables */
	private double latitude, longitude;
	private long time;
	
	/* Constructor */
	public FlightPoint(double latitude, double longitude) {
		this(latitude, longitude, new Date().getTime());
	}
	
	/* Constructor */
	public FlightPoint(double latitude, double longitude, long time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
	}
	
	/** @return Point's latitude. */
	public double getLatitude() {
		return latitude;
	}
	
	/** @return Point's longitude. */
	public double getLongitude() {
		return longitude;
	}
	
	/** @return Point's time when he has been taked. */
	public long getTime() {
		return time;
	}
	
	/** @return A {@link JsonObject} version of this object. */
	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.put("latitude", latitude);
		jsonObject.put("longitude", longitude);
		jsonObject.put("time", time);
		
		return jsonObject;
	}
	
}