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
	
	@ConfigProperty(defaultValue = "8081", type = ConfigProperty.PropertyType.INTEGER, file = "config", key = "webinterface.picture.port")
	public static int WEB_INTERFACE_PICTURE_PORT;
	
	@ConfigProperty(defaultValue = "8082", type = ConfigProperty.PropertyType.INTEGER, file = "config", key = "websocket.port")
	public static int WEB_SOCKET_PORT;

	@ConfigProperty(defaultValue = "storage/pictures/", file = "config", key = "webinterface.picture.storage.directory")
	public static String WEB_INTERFACE_PICTURE_STORAGE_DIRECTORY;
	
	@ConfigProperty(defaultValue = "storage/web_settings.json", file = "config", key = "websettings.storage.file")
	public static String WEB_SETTINGS_STORAGE_FILE;
	
	@ConfigProperty(defaultValue = "storage/qualities/values.db", file = "config", key = "api.physical-quantities.storage.database.file")
	public static String API_PHYSICAL_QUALITIES_STORAGE_DATABASE_FILE;
	
	@ConfigProperty(defaultValue = "storage/qualities/list.json", file = "config", key = "api.physical-quantities.list")
	public static String API_PHYSICAL_QUALITIES_LIST;
	
	@ConfigProperty(defaultValue = "60", type = ConfigProperty.PropertyType.INTEGER, file = "config", key = "api.physical-quantities.request.max-value-size")
	public static int API_PHYSICAL_QUALITIES_REQUEST_MAX_VALUE_SIZE;
	
	@ConfigProperty(defaultValue = "100", type = ConfigProperty.PropertyType.INTEGER, file = "config", key = "websocket.server.connection.lost.timeout")
	public static int WEB_SOCKET_SERVER_CONNECTION_LOST_TIMEOUT;
	
	/** Initialize config file. */
	public static void initialize() {
		CONFIG = (Config) initialize(Config.class);
	}
	
	/** @return Config's singleton */
	public static Config getConfig() {
		return CONFIG;
	}
	
}