package caceresenzo.server.drone.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.random.RandomString;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.server.drone.Application;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.flight.models.FlightPoint;

@RestController
public class DroneApi {
	
	/* Managers */
	private FlightController flightController;
	
	/* Tests */
	private Thread testThread;
	
	/* Constructor */
	public DroneApi() {
		this.flightController = FlightController.getFlightController();
	}
	
	@GetMapping(value = "/flight")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flight() {
		Map<String, Object> response = new HashMap<>();
		
		response.put("active", flightController.isFlightActive());
		
		if (flightController.isFlightActive()) {
			JsonObject flightPart = new JsonObject();
			
			Flight flight = flightController.getCurrentFlight();
			
			flightPart.put("name", flight.getName());
			flightPart.put("local_file", flight.getLocalFile().getName());
			flightPart.put("start", flight.getStart());
			flightPart.put("points", flight.getPoints());
			
			response.put("flight", flightPart);
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * GARBAGE CODE
	 */
	@GetMapping(value = "/flight/test/{eta}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flightTest(@PathVariable("eta") String eta) {
		Map<String, Object> response = new HashMap<>();
		
		if (eta.equalsIgnoreCase("stop")) {
			testThread = null; /* Make it crash */
			
			response.put("result", "stop");
		} else {
			testThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Random random = new Random();
					Flight flight = new Flight(new File(new RandomString().nextString()), "hello");
					
					flightController.start(flight);
					
					while (!testThread.isInterrupted()) {
						flight.addPoint(new FlightPoint(random.nextFloat(), random.nextFloat()));
						
						ThreadUtils.sleep(2000);
					}
				}
			});
			
			testThread.start();
			
			response.put("result", "start");
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> apiInfo() {
		Map<String, Object> response = new HashMap<>();
		
		Map<String, Object> versionPart = new HashMap<>();
		
		versionPart.put("id", Application.VERSION.get(false));
		versionPart.put("type", Application.VERSION.getType().name());
		
		response.put("api", "Drone API");
		response.put("author", "Enzo CACERES");
		response.put("version", versionPart);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}