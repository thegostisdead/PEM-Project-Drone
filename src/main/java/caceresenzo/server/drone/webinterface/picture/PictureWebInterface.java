package caceresenzo.server.drone.webinterface.picture;

import caceresenzo.server.drone.webinterface.picture.workers.SocketServerThread;

public class PictureWebInterface {
	
	/* Variables */
	private SocketServerThread socketServerThread;
	
	/* Constructor */
	public PictureWebInterface() {
		this.socketServerThread = new SocketServerThread();
	}
	
	/** Start the {@link SocketServerThread}. */
	public void start() {
		socketServerThread.start();
	}
	
}