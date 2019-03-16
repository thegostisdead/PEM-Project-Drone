package caceresenzo.server.drone.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.server.drone.Application;

@RestController
public class DroneApi {
	
	@GetMapping(value = "/")
	public synchronized ResponseEntity<Map<String, Object>> apiInfo() {
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