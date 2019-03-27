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
	private PictureManager pictureManager;
	
	/* Variables */
	private SocketServerThread socketServerThread;
	
	/* Constructor */
	public PictureWebInterface() {
		this.exchangeManager = ExchangeManager.getExchangerManager();
		this.flightController = FlightController.getFlightController();
		this.pictureManager = PictureManager.getPictureManager();
		
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
		Flight flight = null;
		
		JsonObject filePart = picture.toJsonObject();
		
		jsonObject.put("file", filePart);
		
		jsonObject.put("flight", null);
		if (flightController.isFlightActive()) {
			JsonObject flightPart = new JsonObject();
			
			flight = flightController.getCurrentFlight();
			
			flightPart.put("name", flight.getName());
			flightPart.put("local_file", flight.getLocalFile().getName());
			
			jsonObject.put("flight", flightPart);
			
			picture.attachFlight(flight);
		}
		
		try {
			FileUtils.writeStringToFile(jsonObject.toJsonString(), picture.toPropertyFile());
		} catch (IOException exception) {
			LOGGER.error("Failed to save property file.", exception);
			
			return; /* Can't continue */
		}
		
		Picture.includeRemote(picture, filePart);
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_PICTURE_DOWNLOA_FINISHED, jsonObject);
		
		pictureManager.satisfy(picture.toReference(), picture, flight);
	}
	
	public SocketServerThread getSocketServerThread() {
		return socketServerThread;
	}
	
}