package caceresenzo.server.drone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.qualities.QualityManager;
import caceresenzo.server.drone.utils.Destroyable;
import caceresenzo.server.drone.utils.Initializable;
import caceresenzo.server.drone.webinterface.picture.PictureManager;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;
import caceresenzo.server.drone.websocket.DroneWebSocketServer;
import caceresenzo.server.drone.websocket.ExchangeManager;

@SpringBootApplication
@EnableWebMvc
public class Application implements WebMvcConfigurer {
	
	/* Constants */
	public static final Version VERSION = new Version("0.1", VersionType.BETA);
	
	/* Static */
	public static ConfigurableApplicationContext CONTEXT;
	
	/* *-ables */
	private List<Object> instanced;
	
	/* Variables */
	private DroneWebSocketServer droneWebSocketServer;
	private PictureWebInterface pictureWebInterface;
	
	/* Constructor */
	public Application() {
		this.instanced = new ArrayList<>();

		this.instanced.add(FlightController.create());
		this.instanced.add(ExchangeManager.create());
		this.instanced.add(PictureManager.create());
		this.instanced.add(QualityManager.create());
		
		this.droneWebSocketServer = new DroneWebSocketServer();
		this.pictureWebInterface = new PictureWebInterface();
		
		initialize();
		start();
	}
	
	/** Initialize instanced objects. */
	private void initialize() {
		instanced.forEach(object -> {
			if (object instanceof Initializable) {
				((Initializable) object).initialize();
			}
		});
	}
	
	/** Start the local servers. */
	private void start() {		
		droneWebSocketServer.start();
		pictureWebInterface.start();
	}
	
	@PreDestroy
	public void destroy() {
		droneWebSocketServer.end();
		pictureWebInterface.getSocketServerThread().end();
		
		instanced.forEach(object -> {
			if (object instanceof Destroyable) {
				((Destroyable) object).destroy();
			}
		});
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
		CONTEXT = application.run(args);
		
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String input = scanner.nextLine();
			
			if ("exit".equals(input)) {
				CONTEXT.close();
				break;
			}
		}
		
		scanner.close();
	}
	
}