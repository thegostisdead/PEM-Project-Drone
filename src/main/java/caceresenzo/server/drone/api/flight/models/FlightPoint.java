package caceresenzo.server.drone.api.flight.models;

import static caceresenzo.server.drone.api.flight.models.Flight.JSON_KEY_POSITIONS_ITEM_ID;
import static caceresenzo.server.drone.api.flight.models.Flight.JSON_KEY_POSITIONS_ITEM_LATITUDE;
import static caceresenzo.server.drone.api.flight.models.Flight.JSON_KEY_POSITIONS_ITEM_LONGITUDE;
import static caceresenzo.server.drone.api.flight.models.Flight.JSON_KEY_POSITIONS_ITEM_ALTITUDE;
import static caceresenzo.server.drone.api.flight.models.Flight.JSON_KEY_POSITIONS_ITEM_TIME;

import java.util.Date;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.api.flight.FlightController;

public class FlightPoint implements Comparable<FlightPoint> {
	
	/* Variables */
	private final String latitude, longitude, altitude;
	private final long time, id;
	
	/* Constructor */
	public FlightPoint() {
		this(null, null, null);
	}
	
	/* Constructor */
	public FlightPoint(String latitude, String longitude, String altitude) {
		this(latitude, longitude, altitude, new Date().getTime(), FlightController.getFlightController().getCurrentFlight().getPoints().size() + 1);
	}
	
	/* Constructor */
	public FlightPoint(String latitude, String longitude, String altitude, long time, long id) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.time = time;
		this.id = id;
	}
	
	@Override
	public int compareTo(FlightPoint other) {
		if (getId() != other.getId()) {
			return Long.signum(getId() - other.getId());
		}
		
		return Long.signum(getTime() - other.getTime());
	}
	
	/** @return Point's latitude. */
	public String getLatitude() {
		return latitude;
	}
	
	/** @return Point's longitude. */
	public String getLongitude() {
		return longitude;
	}
	
	/** @return Point's altitude. */
	public String getAltitude() {
		return altitude;
	}
	
	/** @return Point's time when he has been taked. */
	public long getTime() {
		return time;
	}
	
	/** @return Point's supposed id. */
	public long getId() {
		return id;
	}
	
	/** @return A {@link JsonObject} version of this object. */
	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.put(JSON_KEY_POSITIONS_ITEM_LATITUDE, latitude);
		jsonObject.put(JSON_KEY_POSITIONS_ITEM_LONGITUDE, longitude);
		jsonObject.put(JSON_KEY_POSITIONS_ITEM_ALTITUDE, altitude);
		jsonObject.put(JSON_KEY_POSITIONS_ITEM_TIME, time);
		jsonObject.put(JSON_KEY_POSITIONS_ITEM_ID, id);
		
		return jsonObject;
	}
	
}