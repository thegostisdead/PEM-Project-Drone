package caceresenzo.server.drone.api.flight.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import caceresenzo.server.drone.api.flight.FlightController;

public class Flight {
	
	/* Managers */
	private FlightController controller;
	
	/* Variables */
	private final File file;
	private String name;
	private long start, end;
	private boolean active;
	private final List<FlightPoint> points;
	
	/* Constructor */
	public Flight(File file, String name) {
		this.file = file;
		this.name = name;
		this.points = new ArrayList<>();
	}
	
	/**
	 * Set this {@link Flight} as active and set the start time to now.
	 * 
	 * @return Itself.
	 */
	public Flight activate(FlightController controller) {
		this.controller = controller;
		
		this.active = true;
		this.start = new Date().getTime();
		
		return this;
	}
	
	/**
	 * Set this {@link Flight} as finished and set the stop time to now.
	 * 
	 * @return Itself.
	 */
	public Flight finish() {
		this.active = false;
		this.end = new Date().getTime();
		
		return this;
	}
	
	/**
	 * Add a {@link FlightPoint} to this {@link Flight}.
	 * 
	 * @param flightPoint
	 *            Target point to add.
	 * @return {@link List#add(Object)}.
	 * @throws IllegalStateException
	 *             If this {@link Flight} is not active.
	 */
	public boolean addPoint(FlightPoint flightPoint) {
		if (!isActive()) {
			throw new IllegalStateException("Flight is not active.");
		}
		
		controller.sendNewPointPosition(flightPoint);
		
		return points.add(flightPoint);
	}
	
	/** @return {@link Flight}'s name. */
	public String getName() {
		return name;
	}
	
	/** @return Time when the {@link Flight} started. */
	public long getStart() {
		return start;
	}

	/** @return Time when the {@link Flight} stopped. */
	public long getEnd() {
		return end;
	}

	/** @return {@link Flight}'s duration (end - start). */
	public long getDuration() {
		return end - start;
	}

	/** @return {@link Flight}'s current active state. */
	public boolean isActive() {
		return active;
	}

	/** @return {@link Flight}'s local storage file. */
	public File getLocalFile() {
		return file;
	}

	/** @return {@link Flight}'s point list. */
	public List<FlightPoint> getPoints() {
		return points;
	}
	
}