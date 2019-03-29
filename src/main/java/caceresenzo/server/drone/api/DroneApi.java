package caceresenzo.server.drone.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.json.JsonArray;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.random.RandomString;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.server.drone.Application;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.flight.models.FlightPoint;

@CrossOrigin(origins = "*")
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
	
	@GetMapping(value = "/flights")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flights() {
		Map<String, Object> response = new HashMap<>();
		
		JsonObject currentPart = new JsonObject();
		currentPart.put("active", flightController.isFlightActive());
		
		if (flightController.isFlightActive()) {
			currentPart.put("flight", flightController.getCurrentFlight().toMoreDetailedJsonObject());
		}
		
		List<Flight> sortedFlights = new ArrayList<>(flightController.getAllFlight().values());
		sortedFlights.sort(new Comparator<Flight>() {
			@Override
			public int compare(Flight o1, Flight o2) {
				return Long.signum(o2.getStart() - o1.getStart());
			}
		});
		
		JsonArray allFlightJsonArray = new JsonArray();
		for (Flight flight : sortedFlights) {
			allFlightJsonArray.add(flight.toMoreDetailedJsonObject());
		}
		allFlightJsonArray.sort(new Comparator<Object>() {
			@Override
			public int compare(Object object1, Object object2) {
				return 0;
			}
		});
		
		response.put("current", currentPart);
		response.put("all", allFlightJsonArray);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/flights/actual")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> actualFlight() {
		return null;
	}
	
	@GetMapping(value = "/flights/detail/{local_file}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> detailFlight() {
		return null;
	}
	
	@PostMapping(value = "/flight/position/add")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flightAddNewPosition(@RequestBody FlightPoint body) {
		Map<String, Object> response = new HashMap<>();
		
		response.put("point", body);
		response.put("success", flightController.isFlightActive());
		
		if (flightController.isFlightActive()) {
			flightController.getCurrentFlight().addPoint(body);
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/flight/start/{name}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flightStart(@PathVariable("name") String name) {
		Map<String, Object> response = new HashMap<>();
		
		if (flightController.isFlightActive()) {
			flightController.stop();
		}
		
		flightController.start(new Flight(name));
		
		response.put("result", "ok");
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/flight/stop")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> flightStop() {
		Map<String, Object> response = new HashMap<>();

		response.put("result", "failed");
		
		if (flightController.isFlightActive()) {
			flightController.stop();
			response.put("result", "ok");
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
						flight.addPoint(new FlightPoint(String.valueOf(random.nextFloat()), String.valueOf(random.nextFloat())));
						
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