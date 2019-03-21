package caceresenzo.server.drone.webinterface.picture;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.webinterface.picture.models.Picture;
import caceresenzo.server.drone.webinterface.picture.workers.SocketServerThread;
import caceresenzo.server.drone.websocket.ExchangeManager;

public class PictureWebInterface {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(PictureWebInterface.class);
	
	/* Managers */
	private ExchangeManager exchangeManager;
	
	/* Variables */
	private SocketServerThread socketServerThread;
	
	/* Constructor */
	public PictureWebInterface() {
		this.exchangeManager = ExchangeManager.getExchangerManager();
		this.socketServerThread = new SocketServerThread(this);
	}
	
	/** Start the {@link SocketServerThread}. */
	public void start() {
		socketServerThread.start();
	}
	
	/**
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