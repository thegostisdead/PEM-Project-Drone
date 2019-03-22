package caceresenzo.server.drone;

import java.io.File;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;
import caceresenzo.server.drone.websocket.DroneWebSocketServer;

@SpringBootApplication
public class Application {
	
	/* Constants */
	public static final Version VERSION = new Version("0.1", VersionType.BETA);
	
	/* Variables */
	private DroneWebSocketServer droneWebSocketServer;
	private PictureWebInterface pictureWebInterface;
	
	/* Constructor */
	public Application() {
		this.droneWebSocketServer = new DroneWebSocketServer();
		this.pictureWebInterface = new PictureWebInterface();
		
		start();
		
		FlightController.getFlightController().start(new Flight(new File("hello"), "test"));
	}
	
	/** Start the local servers. */
	private void start() {
		droneWebSocketServer.start();
		pictureWebInterface.start();
	}

	/* Main */
	public static void main(String[] args) {
		Config.initialize();
		
		SpringApplication application = new SpringApplication(Application.class);
		application.setDefaultProperties(Collections.singletonMap("server.port", Config.API_PORT));
		application.run(args);
	}
	
}