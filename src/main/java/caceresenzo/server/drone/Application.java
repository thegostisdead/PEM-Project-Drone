package caceresenzo.server.drone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	@Override
	public void run(String... args) throws Exception {
		// TODO Parse console commands
	}
	
	/* Main */
	public static void main(String[] args) {
		Config.initialize();
		
		SpringApplication.run(Application.class, args);
	}
	
}