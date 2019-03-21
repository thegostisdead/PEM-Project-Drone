package caceresenzo.server.drone.webinterface.picture.workers;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.stream.StreamUtils;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;

public class SocketServerThread extends Thread {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(SocketServerThread.class);
	
	/* Variables */
	private final PictureWebInterface pictureWebInterface;
	private final int port;
	
	/* Constructor */
	public SocketServerThread(PictureWebInterface pictureWebInterface) {
		this(pictureWebInterface, Config.WEB_INTERFACE_PICTURE_PORT);
	}
	
	/* Constructor */
	public SocketServerThread(PictureWebInterface pictureWebInterface, int port) {
		this.pictureWebInterface = pictureWebInterface;
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
					SocketProcessorThread.create(pictureWebInterface, serverSocket.accept());
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