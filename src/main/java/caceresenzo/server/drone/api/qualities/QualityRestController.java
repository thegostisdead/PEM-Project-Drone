package caceresenzo.server.drone.api.qualities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.qualities.models.PhysicalQuality;
import caceresenzo.server.drone.api.qualities.models.ValueHolder;

@RestController
public class QualityRestController {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(QualityRestController.class);
	
	/* Managers */
	private FlightController flightController;
	private QualityManager qualityManager;
	
	/* Constructor */
	public QualityRestController() {
		super();
		
		this.flightController = FlightController.getFlightController();
		this.qualityManager = QualityManager.getQualityManager();
	}
	
	@GetMapping(value = "/qualities")
	public ResponseEntity<Map<String, Object>> qualitiesGet() {
		Map<String, Object> response = new HashMap<>();
		
		JsonObject qualitiesPart = new JsonObject();
		response.put("qualities", qualitiesPart);
		
		JsonObject flightsPart = new JsonObject();
		response.put("flights", flightsPart);
		
		JsonObject currentflightsPart = new JsonObject();
		flightsPart.put("current", currentflightsPart);
		
		JsonObject allflightsPart = new JsonObject();
		flightsPart.put("all", allflightsPart);
		
		for (PhysicalQuality physicalQuality : qualityManager.getRegistry().getLoaded(false)) {
			Map<String, Object> qualityMap = new HashMap<>();
			
			qualityMap.put(QualityRegistry.JSON_KEY_PHYSICAL_QUALITY_NAME, physicalQuality.getName());
			qualityMap.put(QualityRegistry.JSON_KEY_PHYSICAL_QUALITY_UNIT, physicalQuality.getUnit());
			qualityMap.put(QualityRegistry.JSON_KEY_PHYSICAL_QUALITY_USE_GRAPH, physicalQuality.isUseGraph());
			qualityMap.put(QualityRegistry.JSON_KEY_PHYSICAL_QUALITY_DISABLE_STORAGE, physicalQuality.isStorageDisabled());
			
			qualitiesPart.put(physicalQuality.getName(), qualityMap);
		}
		
		boolean active = flightController.isFlightActive();
		currentflightsPart.put("active", active);
		if (active) {
			currentflightsPart.put("flight", createQualityMap(flightController.getCurrentFlight()));
		}
		
		for (Flight flight : flightController.getAllFlight().values()) {
			allflightsPart.put(flight.getLocalFileName(), createQualityMap(flight));
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/qualities/current")
	public ResponseEntity<Map<String, Object>> qualitiesCurrentGet() {
		Map<String, Object> response = new HashMap<>();
		
		boolean active = flightController.isFlightActive();
		response.put("active", active);
		if (active) {
			response.put("flight", createQualityMap(flightController.getCurrentFlight()));
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/qualities/push")
	public ResponseEntity<Object> qualitiesPost(@RequestBody Map<String, List<ValueHolder>> body) {
		Map<String, Object> response = new HashMap<>();
		
		if (flightController.isFlightActive()) {
			int addedEntriesCount = 0;
			
			for (Entry<String, List<ValueHolder>> entry : body.entrySet()) {
				String targetQuality = entry.getKey();
				List<ValueHolder> holders = entry.getValue();
				
				PhysicalQuality correspondingPhysicalQuality = null;
				
				for (PhysicalQuality physicalQuality : qualityManager.getRegistry().getLoaded(false)) {
					if (physicalQuality.getName().equals(targetQuality)) {
						correspondingPhysicalQuality = physicalQuality;
						break;
					}
				}
				
				if (correspondingPhysicalQuality == null) {
					continue;
				}
				
				for (ValueHolder valueHolder : holders) {
					if (valueHolder.areDataValid()) {
						addedEntriesCount++;
						qualityManager.insertNewValues(correspondingPhysicalQuality, valueHolder);
					}
				}
			}
			
			response.put("added_entries_count", addedEntriesCount);
			response.put("success", true);
			
			qualityManager.sendToSocket(body);
		} else {
			return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
		}
		
		response.put("body", body);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	private Map<String, Object> createQualityMap(Flight flight) {
		Map<String, Object> map = new HashMap<>();
		
		for (PhysicalQuality physicalQuality : qualityManager.getRegistry().getLoaded(false)) {
			List<ValueHolder> values = qualityManager.getCache(flight).get(physicalQuality);
			
			map.put(physicalQuality.getName(), values);
		}
		
		return map;
	}
	
}