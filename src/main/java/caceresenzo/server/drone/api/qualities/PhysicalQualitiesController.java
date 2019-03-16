package caceresenzo.server.drone.api.qualities;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonArray;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.qualities.models.PhysicalQuality;
import caceresenzo.server.drone.api.qualities.models.ValueHolder;
import caceresenzo.server.drone.storage.SqliteStorage;

@RestController
public class PhysicalQualitiesController {
	
	/* Constants */
	public static final String JSON_KEY_PHYSICAL_QUALITY_NAME = "name";
	public static final String JSON_KEY_PHYSICAL_QUALITY_UNIT = "unit";
	public static final String JSON_KEY_PHYSICAL_QUALITY_VALUES = "values";
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(PhysicalQualitiesController.class);
	
	/* Variables */
	private final SqliteStorage sqliteStorage;
	private final List<PhysicalQuality> qualities;
	private final Map<PhysicalQuality, List<ValueHolder>> cache;
	
	/* Constructor */
	public PhysicalQualitiesController() {
		super();
		
		this.sqliteStorage = new SqliteStorage(Config.API_PHYSICAL_QUALITIES_STORAGE_DATABASE_FILE);
		this.qualities = new ArrayList<>();
		this.cache = new HashMap<>();
		
		initialize();
	}
	
	/** Load qualities, then connect to the database and check tables, and call {@link #prepareCache()}. */
	private void initialize() {
		loadQualities();
		
		if (sqliteStorage.connect()) {
			for (PhysicalQuality physicalQuality : qualities) {
				sqliteStorage.execute(String.format("CREATE TABLE IF NOT EXISTS `%s`( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `date` INTEGER NOT NULL, `content` INTEGER NOT NULL);", physicalQuality.getName()));
			}
		}
		
		prepareCache();
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
				file.mkdirs();
				file.createNewFile();
				
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
	
	/** Prepare the cache map and call {@link #buildCache()} to fill it. */
	private void prepareCache() {
		for (PhysicalQuality physicalQuality : qualities) {
			cache.put(physicalQuality, new ArrayList<>());
		}
		
		buildCache();
	}
	
	/** Clean the cache and call {@link #buildCache()} to fill it up again with new values. */
	private void invalidateCache() {
		for (PhysicalQuality physicalQuality : qualities) {
			cache.get(physicalQuality).clear();
		}
		
		buildCache();
	}
	
	/** Build the cache from the data available in the database. */
	private void buildCache() {
		for (PhysicalQuality physicalQuality : qualities) {
			ResultSet resultSet = sqliteStorage.query(String.format("SELECT * FROM `%s` ORDER BY `id` DESC LIMIT %s;", physicalQuality.getName(), Config.API_PHYSICAL_QUALITIES_REQUEST_MAX_VALUE_SIZE));
			
			if (resultSet == null) {
				continue;
			}
			
			try {
				while (resultSet.next()) {
					long date = resultSet.getLong("date");
					String content = resultSet.getString("content");
					
					cache.get(physicalQuality).add(new ValueHolder(date, content));
				}
			} catch (SQLException exception) {
				LOGGER.error("Failed to iterate over database selected content.", exception);
			}
		}
	}
	
	/**
	 * Insert the content of a {@link ValueHolder} to the database.
	 * 
	 * @param physicalQuality
	 *            Quality that is supposed to represent the value.
	 * @param valueHolder
	 *            Target {@link ValueHolder}.
	 */
	private synchronized void insertNewValues(PhysicalQuality physicalQuality, ValueHolder valueHolder) {
		sqliteStorage.execute(String.format("INSERT INTO `%s` (date, content) VALUES (%s, \"%s\");", physicalQuality.getName(), valueHolder.getDate(), valueHolder.getContent()));
	}
	
	@GetMapping(value = "/qualities")
	public synchronized ResponseEntity<Map<String, Object>> qualitiesGet() {
		Map<String, Object> response = new HashMap<>();
		
		for (PhysicalQuality physicalQuality : qualities) {
			List<ValueHolder> values = cache.get(physicalQuality);
			
			Map<String, Object> qualityMap = new HashMap<>();
			
			qualityMap.put(JSON_KEY_PHYSICAL_QUALITY_NAME, physicalQuality.getName());
			qualityMap.put(JSON_KEY_PHYSICAL_QUALITY_UNIT, physicalQuality.getUnit());
			qualityMap.put(JSON_KEY_PHYSICAL_QUALITY_VALUES, values);
			
			response.put(physicalQuality.getName(), qualityMap);
		}
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/qualities/push")
	public synchronized ResponseEntity<Object> qualitiesPost(@RequestBody Map<String, List<ValueHolder>> body) {
		int addedEntriesCount = 0;
		
		for (Entry<String, List<ValueHolder>> entry : body.entrySet()) {
			String targetQuality = entry.getKey();
			List<ValueHolder> holders = entry.getValue();
			
			PhysicalQuality correspondingPhysicalQuality = null;
			
			for (PhysicalQuality physicalQuality : qualities) {
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
					insertNewValues(correspondingPhysicalQuality, valueHolder);
				}
			}
		}
		
		invalidateCache();
		
		Map<String, Object> response = new HashMap<>();
		response.put("added_entries_count", addedEntriesCount);
		response.put("body", body);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "/qualities/list")
	public synchronized ResponseEntity<Object> qualitiesList() {
		Map<String, Object> response = new HashMap<>();
		
		response.put("loaded", qualities);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}