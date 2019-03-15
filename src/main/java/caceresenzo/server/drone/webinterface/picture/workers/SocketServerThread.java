package caceresenzo.server.drone.webinterface.picture.workers;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.stream.StreamUtils;
import caceresenzo.server.drone.Config;

public class SocketServerThread extends Thread {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(SocketServerThread.class);
	
	/* Variables */
	private final int port;
	
	/* Constructor */
	public SocketServerThread() {
		this(Config.WEB_INTERFACE_PICTURE_PORT);
	}
	
	/* Constructor */
	public SocketServerThread(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(port);
			
			LOGGER.info("Picture WebInterface is running!");
			
			while (true) {
				try {
					SocketProcessorThread.create(serverSocket.accept());
				} catch (Exception exception) {
					LOGGER.error("Failed accept incoming socket.", exception);
				}
			}
			
		} catch (IOException exception) {
			LOGGER.error("Failed to run socket server.", exception);
		}
		
		StreamUtils.close(serverSocket);
	}
	
}