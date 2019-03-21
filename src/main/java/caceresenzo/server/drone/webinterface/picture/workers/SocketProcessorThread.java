package caceresenzo.server.drone.webinterface.picture.workers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.stream.StreamUtils;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;
import caceresenzo.server.drone.webinterface.picture.models.Picture;

public class SocketProcessorThread extends Thread {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(SocketProcessorThread.class);
	
	/* Variables */
	private final Socket socket;
	private final PictureWebInterface pictureWebInterface;
	
	/* Constructor */
	private SocketProcessorThread(PictureWebInterface pictureWebInterface, Socket socket) {
		this.pictureWebInterface = pictureWebInterface;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		final String ipAdress = socket.getInetAddress().getHostAddress();
		
		OutputStream outStream = null;
		try {
			InputStream inputStream = socket.getInputStream();
			
			/* Reading header */
			StringBuilder builder = new StringBuilder();
			while (true) {
				int next = inputStream.read();
				
				if (next == '\n') {
					break;
				}
				
				builder.append((char) next);
			}
			
			String header = builder.toString();
			
			Picture picture = Picture.fromHeader(header);
			
			if (!StringUtils.validate(picture.getName())) {
				throw new IllegalStateException("The image name is null.");
			}
			
			/* Creating target */
			File targetFile = picture.toFile();;
			targetFile.mkdirs();
			targetFile.delete();
			targetFile.createNewFile();
			
			LOGGER.info("Starting download... (file = \"{}\", size = {} bytes, from = {})", targetFile.getName(), picture.getFileLength(), ipAdress);
			
			/* Start transfer */
			outStream = new FileOutputStream(targetFile);
			
			long received = 0;
			while (++received != picture.getFileLength()) {
				int next = inputStream.read();
				
				if (next == -1 && socket.isConnected()) {
					throw new IllegalStateException("Failed to fully read stream.");
				}
				
				outStream.write(next);
			}
			
			LOGGER.info("Downloaded image \"{}\".", targetFile.getName());
			
			pictureWebInterface.onPictureDownloadFinished(picture);
		} catch (Exception exception) {
			LOGGER.error("Failed to process socket. (ip = " + ipAdress + ")", exception);
		} finally {
			StreamUtils.close(outStream);
		}

		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (Exception exception) {
			LOGGER.error("Failed to close socket.", exception);
		}
	}
	
	/**
	 * Create a {@link SocketProcessorThread} from a {@link Socket} object.
	 * 
	 * @param socket
	 *            Target {@link Socket}, will do nothing if null.
	 */
	public static void create(PictureWebInterface pictureWebInterface, Socket socket) {
		if (socket == null) {
			return;
		}
		
		new SocketProcessorThread(pictureWebInterface, socket).start();
	}
	
}