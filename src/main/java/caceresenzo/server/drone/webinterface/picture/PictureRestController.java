package caceresenzo.server.drone.webinterface.picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.json.JsonArray;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.stream.StreamUtils;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.webinterface.picture.models.Picture;

@RestController
public class PictureRestController {
	
	@GetMapping(value = "/storage/pictures")
	public ResponseEntity<JsonObject> picturesList() throws IOException {
		PictureManager pictureManager = PictureManager.getPictureManager();
		FlightController flightController = FlightController.getFlightController();
		
		JsonObject response = new JsonObject();
		
		Map<Flight, List<Picture>> pictureByFlightMap = new HashMap<>();
		for (Picture picture : pictureManager.getPictures().values()) {
			List<Picture> pictures = pictureByFlightMap.get(picture.getAttachedFlight());
			
			if (pictures == null) {
				pictureByFlightMap.put(picture.getAttachedFlight(), pictures = new ArrayList<>());
			}
			
			pictures.add(picture);
		}
		
		for (Entry<Flight, List<Picture>> entry : pictureByFlightMap.entrySet()) {
			Flight flight = entry.getKey();
			List<Picture> pictures = entry.getValue();
			
			JsonArray picturesJsonArray = new JsonArray();
			pictures.forEach(picture -> picturesJsonArray.add(Picture.includeRemote(picture, picture.toJsonObject())));
			
			String group = "unknown";
			if (flight != null) {
				group = flight.getLocalFileName();
			}
			
			response.put(group, picturesJsonArray);
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/storage/pictures/{file}", produces = "image/*")
	public ResponseEntity<byte[]> getImageAsResponseEntity(@PathVariable("file") String filename) throws IOException {
		Picture picture = Picture.fromName(filename);
		File file = picture.toFile();
		
		if (!file.exists()) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
		}
		
		InputStream inputStream = new FileInputStream(file);
		byte[] media = IOUtils.toByteArray(inputStream);
		StreamUtils.close(inputStream);
		
		return new ResponseEntity<>(media, HttpStatus.OK);
	}
	
}