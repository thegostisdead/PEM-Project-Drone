package caceresenzo.server.drone.webinterface.picture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.webinterface.picture.models.Picture;
import caceresenzo.server.drone.webinterface.picture.models.PictureReference;

public class PictureManager {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(PictureManager.class);
	
	/* Instance */
	private static PictureManager MANAGER;
	
	/* Managers */
	private FlightController flightController;
	
	/* Variables */
	private Map<String, Picture> pictureReferencesMap;
	
	/* Constructor */
	private PictureManager() {
		this.pictureReferencesMap = new HashMap<>();
	}
	
	public void initialize() {
		this.flightController = FlightController.getFlightController();
		
		File directory = new File(Config.WEB_INTERFACE_PICTURE_STORAGE_DIRECTORY);
		File[] files = directory.listFiles((filepath) -> filepath.getName().endsWith(".json"));
		
		JsonParser parser = new JsonParser();
		
		for (File file : files) {
			try {
				JsonObject jsonObject = (JsonObject) parser.parse(StringUtils.fromFile(file));
				
				JsonObject filePart = jsonObject.getJsonObject("file");
				JsonObject flightPart = jsonObject.getJsonObject("flight");
				
				String flightLocalFileName = flightPart.getString("local_file");
				if (!StringUtils.validate(flightLocalFileName)) {
					continue;
				}
				
				Picture picture = Picture.fromJsonObject(filePart);
				
				if (picture != null) {
					PictureReference pictureReference = picture.toReference();
					
					pictureReferencesMap.put(pictureReference.getReference(), picture);
					
					Flight flight = flightController.getFlightByLocalFileName(flightLocalFileName);
					if (flight != null) {
						picture.attachFlight(flight);
					}
				}
			} catch (Exception exception) {
				LOGGER.error("Failed to read picture data file. (file = " + file.getName() + ")", exception);
			}
		}
	}
	
	public Picture satisfy(PictureReference pictureReference) {
		return pictureReferencesMap.get(pictureReference.getReference());
	}
	
	public void satisfy(PictureReference pictureReference, Picture picture, Flight flight) {
		pictureReferencesMap.put(pictureReference.getReference(), picture.attachFlight(flight));
	}
	
	public Map<String, Picture> getPictures() {
		return pictureReferencesMap;
	}
	
	public List<Picture> getPicturesByFlight(Flight flight) {
		List<Picture> pictures = new ArrayList<>();
		
		for (Picture picture : pictureReferencesMap.values()) {
			if (flight.equals(picture.getAttachedFlight())) {
				pictures.add(picture);
			}
		}
		
		return pictures;
	}
	
	public static PictureManager create() {
		return MANAGER = new PictureManager();
	}
	
	public static PictureManager getPictureManager() {
		return Objects.requireNonNull(MANAGER, "Instance is null, did you call create() before ?");
	}
	
}
