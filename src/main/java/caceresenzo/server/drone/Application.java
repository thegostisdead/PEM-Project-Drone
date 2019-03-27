package caceresenzo.server.drone;

import java.io.File;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.flight.models.FlightPoint;
import caceresenzo.server.drone.webinterface.picture.PictureManager;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;
import caceresenzo.server.drone.websocket.DroneWebSocketServer;
import caceresenzo.server.drone.websocket.ExchangeManager;

@SpringBootApplication
@EnableWebMvc
public class Application implements WebMvcConfigurer {
	
	/* Constants */
	public static final Version VERSION = new Version("0.1", VersionType.BETA);
	
	/* Variables */
	private ExchangeManager exchangeManager;
	private FlightController flightController;
	private PictureManager pictureManager;
	private DroneWebSocketServer droneWebSocketServer;
	private PictureWebInterface pictureWebInterface;
	
	/* Constructor */
	public Application() {
		this.exchangeManager = ExchangeManager.create();
		this.flightController = FlightController.create();
		this.pictureManager = PictureManager.create();
		this.droneWebSocketServer = new DroneWebSocketServer();
		this.pictureWebInterface = new PictureWebInterface();
		
		start();
		
		Flight flight = new Flight(new File("hello"), "test");
		
		flightController.start(flight);
		
		flight.addPoint(new FlightPoint("0.123", "3.210", System.currentTimeMillis(), 1));
	}
	
	/** Start the local servers. */
	private void start() {
		flightController.initialize();
		pictureManager.initialize();
		
		droneWebSocketServer.start();
		pictureWebInterface.start();
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}
	
	/* Main */
	public static void main(String[] args) {
		Config.initialize();
		
		SpringApplication application = new SpringApplication(Application.class);
		application.setDefaultProperties(Collections.singletonMap("server.port", Config.API_PORT));
		application.run(args);
	}
	
}