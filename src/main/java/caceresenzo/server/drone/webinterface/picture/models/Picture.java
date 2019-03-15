package caceresenzo.server.drone.webinterface.picture.models;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.parse.ParseUtils;

public class Picture {
	
	/* Constants */
	public static final long INVALID_LENGTH = -1;
	public static final String HEADER_SEPARATOR = ",";
	public static final String ARGUMENT_SEPARATOR = " ";
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(Picture.class);
	
	/* Variables */
	private String name;
	private long fileLength;
	private String latitude, longitude;
	
	/* Constructor */
	private Picture() {
		;
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
	
	/**
	 * Create a {@link Picture} instance from a header string.
	 * 
	 * @param header
	 *            Target header to parse.
	 * @return An instance of a {@link Picture} with header data.
	 */
	public static Picture fromHeader(String header) {
		Objects.requireNonNull(header, "Header can't be null.");
		
		Picture picture = new Picture();
		
		for (String part : header.split(HEADER_SEPARATOR)) {
			String[] arguments = part.split(ARGUMENT_SEPARATOR);
			
			if (arguments.length == 0) {
				continue;
			}
			
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
		
		return picture;
	}
	
}