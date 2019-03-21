package caceresenzo.server.drone.websocket;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.Config;

public class DroneWebSocketServer extends WebSocketServer {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(DroneWebSocketServer.class);
	
	/* Variables */
	private final ExchangeManager exchangeManager;
	
	/* Constructor */
	public DroneWebSocketServer() {
		this(Config.WEB_SOCKET_PORT);
	}
	
	/* Constructor */
	public DroneWebSocketServer(int port) {
		super(new InetSocketAddress(port));
		
		this.exchangeManager = new ExchangeManager(this);
	}
	
	@Override
	public void onStart() {
		LOGGER.info("WebSocket Server started!");
		
		setConnectionLostTimeout(Config.WEB_SOCKET_SERVER_CONNECTION_LOST_TIMEOUT);
	}
	
	@Override
	public void onOpen(WebSocket socket, ClientHandshake handshake) {
		LOGGER.info("Socket {}({}) connected.", ip(socket), port(socket));
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_STATISTICS_ONLY, new JsonObject());
	}
	
	@Override
	public void onClose(WebSocket socket, int code, String reason, boolean remote) {
		LOGGER.info("Socket {}({}) has been disconnected. (reason = \"{}\", remote = {})", ip(socket), port(socket), reason, remote);
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_STATISTICS_ONLY, new JsonObject());
	}
	
	@Override
	public void onMessage(WebSocket socket, String message) {
		LOGGER.info("Received message from socket {}({}): {}", ip(socket), port(socket), message);
	}
	
	@Override
	public void onError(WebSocket socket, Exception exception) {
		if (socket != null) {
			LOGGER.warn("WebSocket with ip " + socket.getRemoteSocketAddress().getAddress().getHostAddress() + " generated an exception.", exception);
		} else {
			LOGGER.warn("An error append with the WebSocket Server.", exception);
		}
	}
	
	private String ip(WebSocket socket) {
		return socket.getRemoteSocketAddress().getAddress().getHostAddress();
	}
	
	private int port(WebSocket socket) {
		return socket.getRemoteSocketAddress().getPort();
	}
	
}