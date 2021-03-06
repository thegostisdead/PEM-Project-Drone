package caceresenzo.server.drone.api.qualities.models;

import java.util.Date;

import caceresenzo.libs.json.JsonObject;

public class ValueHolder {
	
	/* Variables */
	private final long date;
	private final String content;
	
	/** Serializer only constructor */
	public ValueHolder() {
		this(null);
	}
	
	/** Constructor */
	public ValueHolder(String value) {
		this(new Date().getTime(), value);
	}
	
	/** Constructor */
	public ValueHolder(long date, String value) {
		this.date = date;
		this.content = value;
	}
	
	/** @return Weather or not the <code>content</code> is not null and the <code>date</code> is not equal to zero. */
	public boolean areDataValid() {
		return date != 0 && content != null;
	}
	
	/** @return The date when this value has been taked. */
	public long getDate() {
		return date;
	}
	
	/** @return The content of this holder. */
	public String getContent() {
		return content;
	}

	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.put("date", getDate());
		jsonObject.put("content", getContent());
		
		return jsonObject;
	}
	
}