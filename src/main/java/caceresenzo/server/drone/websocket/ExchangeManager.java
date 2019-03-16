package caceresenzo.server.drone.websocket;

import caceresenzo.libs.json.JsonObject;

public class ExchangeManager {
	
	/* Variables */
	private final DroneWebSocketServer webSocketServer;
	
	/* Contructor */
	public ExchangeManager(DroneWebSocketServer webSocketServer) {
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
		JsonObject encapsuled = new JsonObject();
		
		JsonObject statisticsPart = new JsonObject();
		statisticsPart.put("connected", webSocketServer.getConnections().size());
		
		encapsuled.put("identifier", identifier);
		encapsuled.put("data", jsonObject);
		encapsuled.put("statistics", statisticsPart);
		
		webSocketServer.broadcast(encapsuled.toJsonString());
	}
	
}