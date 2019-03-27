package caceresenzo.server.drone.webinterface.picture.models;

import java.io.File;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.random.RandomString;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.flight.models.Flight;

public class Picture {
	
	/* Constants */
	public static final long INVALID_LENGTH = -1;
	public static final String HEADER_SEPARATOR = ",";
	public static final String ARGUMENT_SEPARATOR = " ";
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(Picture.class);
	
	/* Variables */
	private Flight attachedFlight;
	private String name;
	private long fileLength;
	private String latitude, longitude;
	
	/* Constructor */
	private Picture() {
		;
	}
	
	public Picture attachFlight(Flight flight) {
		this.attachedFlight = flight;
		
		return this;
	}
	
	/** @return Picture's name. */
	public String getName() {
		return name;
	}
	
	/** Set new picture's name. */
	private void setName(String name) {
		this.name = name;
	}
	
	/** @return Picture's file length in byte. */
	public long getFileLength() {
		return fileLength;
	}
	
	/** Set new picture's file length in byte. */
	private void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
	
	/** @return Picture's GPS latitude position of the shot. */
	public String getLatitude() {
		return latitude;
	}
	
	/** Set new picture's GPS latitude position. */
	private void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/** @return Picture's GPS longitude position of the shot. */
	public String getLongitude() {
		return longitude;
	}
	
	/** Set new picture's GPS longitude position. */
	private void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public Flight getAttachedFlight() {
		return attachedFlight;
	}
	
	public File toFile() {
		return new File(Config.WEB_INTERFACE_PICTURE_STORAGE_DIRECTORY, getName() + ".jpg");
	}
	
	public File toPropertyFile() {
		return new File(Config.WEB_INTERFACE_PICTURE_STORAGE_DIRECTORY, getName() + ".json");
	}
	
	public PictureReference toReference() {
		return new PictureReference(getName());
	}
	
	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		
		JsonObject filePositionPart = new JsonObject();
		
		jsonObject.put("name", getName());
		jsonObject.put("length", getFileLength());
		jsonObject.put("position", filePositionPart);
		
		filePositionPart.put("latitude", getLatitude());
		filePositionPart.put("longitude", getLongitude());
		
		return jsonObject;
	}
	
	public static JsonObject includeRemote(Picture picture, JsonObject jsonObject) {
		jsonObject.put("remote", String.format("/storage/pictures/%s", picture.getName()));
		
		return jsonObject;
	}
	
	/**
	 * Create a {@link Picture} instance from a header string.
	 * 
	 * @param header
	 *            Target header to parse.
	 * @return An instance of a {@link Picture} with header data.
	 */
	public static Picture fromHeader(String header) {
		Objects.requireNonNull(header, "Header can't be null.");
		
		LOGGER.info("Trying to create a Picture object from header \"{}\"...", header);
		
		Picture picture = new Picture();
		
		for (String part : header.split(HEADER_SEPARATOR)) {
			String[] arguments = part.split(ARGUMENT_SEPARATOR);
			
			switch (arguments[0].toUpperCase()) {
				case "ID": {
					picture.setName(arguments[1]);
					break;
				}
				
				case "LENGTH": {
					picture.setFileLength(ParseUtils.parseLong(arguments[1], INVALID_LENGTH));
					break;
				}
				
				case "GPS": {
					picture.setLatitude(arguments[1]);
					picture.setLongitude(arguments[2]);
					break;
				}
				
				default: {
					LOGGER.warn("Unknown argument: {0}", arguments[0]);
					break;
				}
			}
		}
		
		if (!StringUtils.validate(picture.getName())) {
			picture.setName(new RandomString(50).nextString());
		}
		
		return picture;
	}
	
	public static Picture fromName(String name) {
		Picture picture = new Picture();
		
		picture.setName(name);
		
		return picture;
	}
	
	public static Picture fromJsonObject(JsonObject jsonObject) {
		Picture picture = new Picture();
		
		picture.setName(jsonObject.getString("name"));
		picture.setFileLength(jsonObject.getLong("length"));
		
		JsonObject positionPart = jsonObject.getJsonObject("position");
		
		picture.setLatitude(positionPart.getString("latitude"));
		picture.setLongitude(positionPart.getString("longitude"));
		
		return picture;
	}
	
}