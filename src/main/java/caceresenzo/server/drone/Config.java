package caceresenzo.server.drone;

import caceresenzo.libs.config.Configuration;
import caceresenzo.libs.config.annotations.ConfigFile;
import caceresenzo.libs.config.annotations.ConfigProperty;
import caceresenzo.libs.config.processor.implementations.PropertiesConfigProcessor;

public class Config extends Configuration {
	
	/* Instances */
	private static Config CONFIG;
	
	@ConfigFile(name = "config", processor = PropertiesConfigProcessor.class)
	public static String CONFIG_FILE = "config.properties";
	
	@ConfigProperty(defaultValue = "8080", type = ConfigProperty.PropertyType.INTEGER, file = "config", key = "api.port")
	public static int API_PORT;
	
	@ConfigProperty(defaultValue = "storage/web_settings.json", file = "config", key = "websettings.storage.file")
	public static String WEB_SETTINGS_STORAGE_FILE;
	
	/** Initialize config file. */
	public static void initialize() {
		CONFIG = (Config) initialize(Config.class);
	}
	
	/** @return Config's singleton */
	public static Config getConfig() {
		return CONFIG;
	}
	
}