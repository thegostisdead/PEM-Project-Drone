package caceresenzo.server.drone.api.qualities.models;

public class PhysicalQuality {
	
	/* Variables */
	private final String unit, name;
	private final boolean useGraph, disableStorage;
	
	/** Constructor */
	public PhysicalQuality(String unit, String name, boolean useGraph, boolean disableStorage) {
		this.unit = unit;
		this.name = name;
		this.useGraph = useGraph;
		this.disableStorage = disableStorage;
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

	/** @return Weather or not the physical quality history should be stored in the database. */
	public boolean isStorageDisabled() {
		return disableStorage;
	}
	
}