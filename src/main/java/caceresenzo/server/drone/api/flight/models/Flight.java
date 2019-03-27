package caceresenzo.server.drone.api.flight.models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.webinterface.picture.PictureManager;

public class Flight {
	
	/* Json Keys */
	public static final String JSON_KEY_NAME = "name";
	public static final String JSON_KEY_START = "start";
	public static final String JSON_KEY_END = "end";
	public static final String JSON_KEY_RUSHED = "rushed";
	public static final String JSON_KEY_POSITIONS = "positions";
	public static final String JSON_KEY_POSITIONS_ITEM_ID = "id";
	public static final String JSON_KEY_POSITIONS_ITEM_TIME = "time";
	public static final String JSON_KEY_POSITIONS_ITEM_POSITION = "position";
	public static final String JSON_KEY_POSITIONS_ITEM_LATITUDE = "latitude";
	public static final String JSON_KEY_POSITIONS_ITEM_LONGITUDE = "longitude";
	
	/* Managers */
	private FlightController controller;
	
	/* Variables */
	private final File file;
	private String name;
	private long start, end;
	private boolean active, rushed;
	private final List<FlightPoint> points;
	
	/* Constructor */
	public Flight(File file, String name) {
		this.file = file;
		this.name = name;
		this.points = new ArrayList<>();
	}
	
	/**
	 * Save the flight to the local file.
	 * 
	 * @throws IOException
	 *             If any I/O error append.
	 */
	public void save() throws IOException {
		FileUtils.writeStringToFile(getLocalFile().getAbsolutePath(), toJsonObject().toJsonString());
	}
	
	/**
	 * Set this {@link Flight} as active and set the start time to now.
	 * 
	 * @return Itself.
	 */
	public Flight activate(FlightController controller) {
		this.controller = controller;
		
		this.active = true;
		this.start = new Date().getTime();
		
		return this;
	}
	
	public Flight rush() {
		setRushedState(true);
		
		return this;
	}
	
	/**
	 * Set this {@link Flight} as finished and set the stop time to now.
	 * 
	 * @return Itself.
	 */
	public Flight finish() {
		this.active = false;
		this.end = new Date().getTime();
		
		return this;
	}
	
	/**
	 * Add a {@link FlightPoint} to this {@link Flight}.
	 * 
	 * @param flightPoint
	 *            Target point to add.
	 * @return {@link List#add(Object)}.
	 */
	public boolean addPoint(FlightPoint flightPoint) {
		if (isActive()) {
			controller.sendNewPointPosition(flightPoint);
		}
		
		return points.add(flightPoint);
	}
	
	/** @return {@link Flight}'s name. */
	public String getName() {
		return name;
	}
	
	/** @return Time when the {@link Flight} started. */
	public long getStart() {
		return start;
	}
	
	/** @return Time when the {@link Flight} stopped. */
	public long getEnd() {
		return end;
	}
	
	/** @return {@link Flight}'s duration (end - start). */
	public long getDuration() {
		return end - start;
	}
	
	/** @return {@link Flight}'s current active state. */
	public boolean isActive() {
		return active;
	}
	
	/** @return {@link Flight}'s local storage file. */
	public File getLocalFile() {
		return file;
	}
	
	/** @return {@link Flight}'s point list. */
	public List<FlightPoint> getPoints() {
		return points;
	}
	
	public boolean isRushed() {
		return rushed;
	}
	
	private void setRushedState(boolean state) {
		this.rushed = state;
	}
	
	private void setStart(long start) {
		this.start = start;
	}
	
	private void setEnd(long end) {
		this.end = end;
	}
	
	public static Flight fromJsonObject(File sourceFile) throws Exception {
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(StringUtils.fromFile(sourceFile));
		
		Flight flight = new Flight(sourceFile, jsonObject.getString(JSON_KEY_NAME));
		
		if (ParseUtils.parseBoolean(jsonObject.get(JSON_KEY_RUSHED), false)) {
			flight.setRushedState(true);
		}
		
		/* Common */
		flight.setStart(ParseUtils.parseLong(jsonObject.get(JSON_KEY_START), 0));
		flight.setEnd(ParseUtils.parseLong(jsonObject.get(JSON_KEY_END), 0));
		
		/* Positions */
		for (Object object : (List<?>) jsonObject.get(JSON_KEY_POSITIONS)) {
			JsonObject positionJsonObject = (JsonObject) object;
			
			int id = ParseUtils.parseInt(positionJsonObject.get(JSON_KEY_POSITIONS_ITEM_ID), 0);
			long time = ParseUtils.parseInt(positionJsonObject.get(JSON_KEY_POSITIONS_ITEM_TIME), 0);
			String latitude = positionJsonObject.getString(JSON_KEY_POSITIONS_ITEM_LATITUDE);
			String longitude = positionJsonObject.getString(JSON_KEY_POSITIONS_ITEM_LONGITUDE);
			
			flight.getPoints().add(new FlightPoint(latitude, longitude, time, id));
		}
		
		return flight;
	}
	
	public JsonObject toMoreDetailedJsonObject() {
		PictureManager pictureManager = PictureManager.getPictureManager();
		
		JsonObject jsonObject = toJsonObject();
		
		jsonObject.put("local_file", getLocalFile().getName());
		
		List<String> pictureReferences = new ArrayList<>();
		pictureManager.getPicturesByFlight(this).forEach(picture -> pictureReferences.add(picture.toReference().getReference()));
		
		jsonObject.put("pictures", pictureReferences);
		
		return jsonObject;
	}
	
	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.put(JSON_KEY_NAME, getName());
		jsonObject.put(JSON_KEY_RUSHED, isRushed());
		jsonObject.put(JSON_KEY_START, getStart());
		jsonObject.put(JSON_KEY_END, getEnd());
		
		List<JsonObject> positions = new ArrayList<>();
		points.forEach(flightPoint -> positions.add(flightPoint.toJsonObject()));
		
		jsonObject.put(JSON_KEY_POSITIONS, positions);
		
		return jsonObject;
	}
	
}