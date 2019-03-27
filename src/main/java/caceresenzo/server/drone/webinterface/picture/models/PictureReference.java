package caceresenzo.server.drone.webinterface.picture.models;

public class PictureReference {
	
	/* Variables */
	private final String reference;
	
	/* Constructor */
	public PictureReference(String reference) {
		this.reference = reference;
	}
	
	/** @return Target {@link Picture}'s name. */
	public String getReference() {
		return reference;
	}
	
}