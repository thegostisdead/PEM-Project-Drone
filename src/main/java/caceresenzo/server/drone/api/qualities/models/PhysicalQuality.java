package caceresenzo.server.drone.api.qualities.models;

public class PhysicalQuality {
	
	/* Variables */
	private final String unit, name;
	
	/** Constructor */
	public PhysicalQuality(String unit, String name) {
		this.unit = unit;
		this.name = name;
	}
	
	/** @return The qualilty's unit. */
	public String getUnit() {
		return unit;
	}
	
	/** @return The qualilty's name. */
	public String getName() {
		return name;
	}
	
}