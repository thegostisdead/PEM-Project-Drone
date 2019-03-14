package caceresenzo.server.drone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class Application implements CommandLineRunner {
	
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