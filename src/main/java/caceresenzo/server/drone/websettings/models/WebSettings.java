package caceresenzo.server.drone.websettings.models;

public class WebSettings {
	
	/* Variables */
	private final String key, value;
	
	/** Serializer only constructor */
	public WebSettings() {
		this(null, null);
	}
	
	/** Constructor */
	public WebSettings(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/** @return The settings's key. */
	public String getKey() {
		return key;
	}
	
	/** @return The settings's value. */
	public String getValue() {
		return value;
	}
	
}