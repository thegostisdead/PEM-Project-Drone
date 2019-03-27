package caceresenzo.server.drone.websocket;

import java.util.Objects;

import caceresenzo.libs.json.JsonObject;

public class ExchangeManager {
	
	/* Constants */
	public static final String IDENTIFIER_STATISTICS_ONLY = "statistics.only";
	public static final String IDENTIFIER_PICTURE_DOWNLOA_FINISHED = "picture.download.finished";
	public static final String IDENTIFIER_FLIGHT_STARTING = "flight.starting";
	public static final String IDENTIFIER_FLIGHT_NEW_POINT = "flight.point.new";
	public static final String IDENTIFIER_FLIGHT_FINISHED = "flight.finished";
	
	/* Instance */
	private static ExchangeManager EXCHANGER;
	
	/* Variables */
	private DroneWebSocketServer webSocketServer;
	
	/* Contructor */
	public ExchangeManager() {
		;
	}
	
	public void attachWebSocketServer(DroneWebSocketServer webSocketServer) {
		this.webSocketServer = webSocketServer;
	}
	
	/**
	 * Send a {@link JsonObject} that will be encapsuled to send even more data about the server.
	 * 
	 * @param identifier
	 *            A recognizable identifier to say what data has been send.
	 * @param jsonObject
	 *            Target {@link JsonObject} to send.
	 */
	public void send(String identifier, JsonObject jsonObject) {
		if (webSocketServer == null) {
			return;
		}
		
		JsonObject encapsuled = new JsonObject();
		
		JsonObject statisticsPart = new JsonObject();
		statisticsPart.put("connected", webSocketServer.getConnections().size());
		
		encapsuled.put("identifier", identifier);
		encapsuled.put("data", jsonObject);
		encapsuled.put("statistics", statisticsPart);
		
		webSocketServer.broadcast(encapsuled.toJsonString());
	}
	
	public static ExchangeManager create() {
		return EXCHANGER = new ExchangeManager();
	}
	
	public static ExchangeManager getExchangerManager() {
		return Objects.requireNonNull(EXCHANGER, "Instance can't be null, did you call create() before ?");
	}
	
}