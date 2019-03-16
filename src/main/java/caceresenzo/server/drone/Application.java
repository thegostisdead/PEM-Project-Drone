package caceresenzo.server.drone;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;
import caceresenzo.server.drone.webinterface.picture.PictureWebInterface;
import caceresenzo.server.drone.websocket.DroneWebSocketServer;

@SpringBootApplication
@EnableAutoConfiguration
public class Application implements CommandLineRunner {
	
	/* Constants */
	public static final Version VERSION = new Version("0.1", VersionType.BETA);
	
	@Override
	public void run(String... args) throws Exception {
		// TODO Parse console commands
	}
	
	/* Main */
	public static void main(String[] args) {
		Config.initialize();
		
		SpringApplication application = new SpringApplication(Application.class);
		application.setDefaultProperties(Collections.singletonMap("server.port", Config.API_PORT));
		application.run(args);
		
		new PictureWebInterface().start();
		new DroneWebSocketServer().start();
	}
	
}