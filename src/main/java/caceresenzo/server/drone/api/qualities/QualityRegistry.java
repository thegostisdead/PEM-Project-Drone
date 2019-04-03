package caceresenzo.server.drone.api.qualities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonArray;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.qualities.models.PhysicalQuality;

@RestController
public class QualityRegistry {
	
	/* Constants */
	public static final String JSON_KEY_PHYSICAL_QUALITY_NAME = "name";
	public static final String JSON_KEY_PHYSICAL_QUALITY_UNIT = "unit";
	public static final String JSON_KEY_PHYSICAL_QUALITY_VALUES = "values";
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(QualityRegistry.class);
	
	/* Variables */
	private final List<PhysicalQuality> qualities;
	
	/* Constructor */
	protected QualityRegistry() {
		super();
		
		this.qualities = new ArrayList<>();
		
		initialize();
	}
	
	private void initialize() {
		loadQualities();
	}
	
	/**
	 * Load the qualities that will be available to use from a configuration file.<br>
	 * If the file not exist or is empty, it will be filled with a sample (temperature).<br>
	 */
	private void loadQualities() {
		try {
			File file = new File(Config.API_PHYSICAL_QUALITIES_LIST);
			String content = file.exists() ? StringUtils.fromFile(file) : null;
			
			if (!file.exists() || !StringUtils.validate(content)) {
				FileUtils.forceFileCreation(file);
				
				JsonArray array = new JsonArray();
				JsonObject sampleQualityJsonObject = new JsonObject();
				
				sampleQualityJsonObject.put(JSON_KEY_PHYSICAL_QUALITY_NAME, "temperature");
				sampleQualityJsonObject.put(JSON_KEY_PHYSICAL_QUALITY_UNIT, "°C");
				
				array.add(sampleQualityJsonObject);
				
				FileUtils.writeStringToFile(array.toJsonString(), file);
				
				loadQualities();
			} else {
				JsonArray array = (JsonArray) new JsonParser().parse(content);
				
				for (Object object : array) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) object;
					
					String name = (String) map.get(JSON_KEY_PHYSICAL_QUALITY_NAME);
					String unit = (String) map.get(JSON_KEY_PHYSICAL_QUALITY_UNIT);
					
					if (StringUtils.validate(name, unit)) {
						qualities.add(new PhysicalQuality(unit, name));
					}
				}
			}
		} catch (Exception exception) {
			LOGGER.error("Failed to load qualities list.", exception);
		}
	}
	
	@GetMapping(value = "/qualities/list")
	public ResponseEntity<Object> qualitiesList() {
		Map<String, Object> response = new HashMap<>();
		
		response.put("loaded", qualities);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	public List<PhysicalQuality> getLoaded() {
		return qualities;
	}
	
}