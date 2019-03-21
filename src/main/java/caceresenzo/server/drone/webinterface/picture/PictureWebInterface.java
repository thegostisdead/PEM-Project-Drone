package caceresenzo.server.drone.webinterface.picture;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.webinterface.picture.models.Picture;
import caceresenzo.server.drone.webinterface.picture.workers.SocketServerThread;
import caceresenzo.server.drone.websocket.DroneWebSocketServer;
import caceresenzo.server.drone.websocket.ExchangeManager;

public class PictureWebInterface {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(PictureWebInterface.class);
	
	/* Managers */
	private ExchangeManager exchangeManager;
	private FlightController flightController;
	
	/* Variables */
	private SocketServerThread socketServerThread;
	
	/* Constructor */
	public PictureWebInterface() {
		this.exchangeManager = ExchangeManager.getExchangerManager();
		this.flightController = FlightController.getFlightController();
		
		this.socketServerThread = new SocketServerThread(this);
	}
	
	/** Start the {@link SocketServerThread}. */
	public void start() {
		socketServerThread.start();
	}
	
	/**
	 * Called when a {@link Picture} has successfully been download.<br>
	 * This function will write a little file containing data relative to the image and will also notify all client connected to the {@link DroneWebSocketServer} that a new picture is ready.
	 * 
	 * @param picture
	 */
	public void onPictureDownloadFinished(Picture picture) {
		JsonObject jsonObject = new JsonObject();
		
		JsonObject filePart = new JsonObject();
		JsonObject filePositionPart = new JsonObject();
		
		filePart.put("name", picture.getName());
		filePart.put("length", picture.getFileLength());
		filePart.put("position", filePositionPart);
		
		filePositionPart.put("latitude", picture.getLatitude());
		filePositionPart.put("longitude", picture.getLongitude());
		
		jsonObject.put("file", filePart);
		
		jsonObject.put("flight", null);
		if (flightController.isFlightActive()) {
			JsonObject flightPart = new JsonObject();
			
			Flight flight = flightController.getCurrentFlight();
			
			flightPart.put("name", flight.getName());
			flightPart.put("local_file", flight.getLocalFile().getName());
			
			jsonObject.put("flight", flightPart);
		}
		
		try {
			FileUtils.writeStringToFile(jsonObject.toJsonString(), picture.toPropertyFile());
		} catch (IOException exception) {
			LOGGER.error("Failed to save property file.", exception);
			
			return; /* Can't continue */
		}
		
		filePart.put("remote", String.format("/storage/pictures/%s", picture.getName()));
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_PICTURE_DOWNLOA_FINISHED, jsonObject);
	}
	
}