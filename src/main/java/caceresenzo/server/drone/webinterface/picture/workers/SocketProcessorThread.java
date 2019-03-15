package caceresenzo.server.drone.webinterface.picture.workers;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.io.IOUtils;

public class SocketProcessorThread extends Thread {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(SocketProcessorThread.class);
	
	/* Variables */
	private final Socket socket;
	
	/* Constructor */
	private SocketProcessorThread(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			System.out.println(new String(IOUtils.read(socket.getInputStream())));
		} catch (Exception exception) {
			LOGGER.error("Failed to process socket. (ip = " + socket.getInetAddress().getHostAddress() + ")", exception);
		}
		
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (Exception exception) {
			LOGGER.error("Failed to close socket.", exception);
		}
	}
	
	public static void create(Socket socket) {
		if (socket == null) {
			return;
		}
		
		new SocketProcessorThread(socket).start();
	}
	
}