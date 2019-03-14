package caceresenzo.server.drone.websettings;

import java.io.File;
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
import caceresenzo.server.drone.websettings.models.WebSettings;

@RestController
public class WebSettingsController {
	
	/* Constants */
	public static final String JSON_KEY_UPDATED_ITEMS = "updated_items";
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(WebSettingsController.class);
	
	/* Variables */
	private Map<String, String> settings;
	
	/* Constructor */
	public WebSettingsController() {
		super();
		
		this.settings = new HashMap<>();
		restoreFromFile();
	}
	
	/** Restore previously added settings key-values back to memory. */
	@SuppressWarnings("unchecked")
	private void restoreFromFile() {
		File file = new File(Config.WEB_SETTINGS_STORAGE_FILE);
		
		if (!file.exists()) {
			try {
				file.mkdirs();
				file.createNewFile();
				FileUtils.writeStringToFile(new JsonArray().toJsonString(), file);
			} catch (Exception exception) {
				LOGGER.error("Failed to create default file.", exception);
			}
			
			return;
		}
		
		JsonArray array;
		try {
			array = (JsonArray) new JsonParser().parse(StringUtils.fromFile(file));
		} catch (Exception exception) {
			LOGGER.error("Failed to parse json of storage file.", exception);
			return;
		}
		
		for (Object object : array) {
			Map<String, String> map = (Map<String, String>) object;
			
			String key = map.get("key");
			String value = map.get("value");
			
			if (StringUtils.validate(key, value)) {
				settings.put(key, value);
			}
		}
	}
	
	/** Save settings in memory to a storage file. */
	private void saveToFile() {
		File file = new File(Config.WEB_SETTINGS_STORAGE_FILE);
		
		if (file.exists()) {
			file.delete();
		}
		
		JsonArray array = new JsonArray();
		
		for (Entry<String, String> entry : settings.entrySet()) {
			JsonObject jsonObject = new JsonObject();
			
			jsonObject.put("key", entry.getKey());
			jsonObject.put("value", entry.getValue());
			
			array.add(jsonObject);
		}
		
		try {
			FileUtils.writeStringToFile(array.toJsonString(), file);
		} catch (Exception exception) {
			LOGGER.error("Failed to save settings to storage file.", exception);
		}
	}
	
	@GetMapping(value = "/settings")
	public synchronized ResponseEntity<List<WebSettings>> settingsGet() {
		List<WebSettings> items = new ArrayList<>();
		
		for (Entry<String, String> entry : settings.entrySet()) {
			items.add(new WebSettings(entry.getKey(), entry.getValue()));
		}
		
		return new ResponseEntity<>(items, HttpStatus.OK);
	}
	
	@PostMapping(value = "/settings")
	public synchronized ResponseEntity<Map<String, Object>> update(@RequestBody List<WebSettings> body) {
		List<WebSettings> updatedItems = new ArrayList<>();
		
		for (WebSettings webSettings : body) {
			if (!settings.containsKey(webSettings.getKey()) || !settings.get(webSettings.getKey()).equals(webSettings.getValue())) {
				settings.put(webSettings.getKey(), webSettings.getValue());
				updatedItems.add(webSettings);
			}
		}
		
		if (!updatedItems.isEmpty()) {
			saveToFile();
		}
		
		Map<String, Object> response = new HashMap<>();
		response.put(JSON_KEY_UPDATED_ITEMS, updatedItems);
		
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
	}
	
}