package caceresenzo.server.drone.api.flight;

import java.util.Objects;

import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.flight.models.FlightPoint;
import caceresenzo.server.drone.websocket.ExchangeManager;

public class FlightController {
	
	/* Instance */
	private static FlightController CONTROLLER;
	
	/* Managers */
	private ExchangeManager exchangeManager;
	
	/* Variables */
	private Flight currentFlight;
	
	/* Constructor */
	public FlightController() {
		this.exchangeManager = ExchangeManager.getExchangerManager();
	}
	
	/**
	 * Start a new {@link Flight}.
	 * 
	 * @param flight
	 *            Target flight.
	 */
	public void start(Flight flight) {
		this.currentFlight = Objects.requireNonNull(flight);
		
		flight.activate(this);
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_FLIGHT_STARTING, new JsonObject());
	}
	
	/**
	 * Send to every connected client the new position of the currently flying device.
	 * 
	 * @param flightPoint
	 *            Target {@link FlightPoint} to send.
	 */
	public void sendNewPointPosition(FlightPoint flightPoint) {
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.put("flight", currentFlight.getName());
		jsonObject.put("position", flightPoint.toJsonObject());
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_FLIGHT_NEW_POINT, jsonObject);
	}
	
	/** @return Weather or not a flight is currently active or not. */
	public boolean isFlightActive() {
		return currentFlight != null;
	}
	
	/**
	 * Check if there is any flight currently active.
	 * 
	 * @throws IllegalStateException
	 *             If there are not flight active.
	 */
	private void checkActiveFlight() {
		if (!isFlightActive()) {
			throw new IllegalStateException("No flight is currently active.");
		}
	}
	
	/**
	 * Will check if there are any current flight active before returning.
	 * 
	 * @return Currently active flight.
	 * @see #checkActiveFlight()
	 */
	public Flight getCurrentFlight() {
		checkActiveFlight();
		
		return currentFlight;
	}
	
	/** @return {@link FlightController}'s singleton. */
	public static FlightController getFlightController() {
		if (CONTROLLER == null) {
			CONTROLLER = new FlightController();
		}
		
		return CONTROLLER;
	}
	
}