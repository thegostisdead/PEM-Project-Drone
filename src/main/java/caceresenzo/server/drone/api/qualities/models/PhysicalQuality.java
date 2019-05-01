package caceresenzo.server.drone.api.qualities.models;

public class PhysicalQuality {
	
	/* Variables */
	private final String unit, name;
	private final boolean useGraph;
	
	/** Constructor */
	public PhysicalQuality(String unit, String name, boolean useGraph) {
		this.unit = unit;
		this.name = name;
		this.useGraph = useGraph;
	}
	
	/** @return The qualilty's unit. */
	public String getUnit() {
		return unit;
	}
	
	/** @return The qualilty's name. */
	public String getName() {
		return name;
	}
	
	/** @return Weather or not the physical quality can be represented using a graph. */
	public boolean isUseGraph() {
		return useGraph;
	}
	
}