package caceresenzo.server.drone.api.flight;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.filesystem.FilenameUtils;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.flight.models.FlightPoint;
import caceresenzo.server.drone.api.qualities.QualityManager;
import caceresenzo.server.drone.utils.Destroyable;
import caceresenzo.server.drone.utils.Initializable;
import caceresenzo.server.drone.websocket.ExchangeManager;

public class FlightController implements Initializable, Destroyable {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(FlightController.class);
	
	/* Instance */
	private static FlightController CONTROLLER;
	
	/* Managers */
	private ExchangeManager exchangeManager;
	private QualityManager qualityManager;
	
	/* Variables */
	private final Map<String, Flight> allFlights;
	private Flight currentFlight;
	
	/* Constructor */
	private FlightController() {
		this.allFlights = new HashMap<>();
	}
	
	public void initialize() {
		exchangeManager = ExchangeManager.getExchangerManager();
		qualityManager = QualityManager.getQualityManager();
		
		File directory = new File(Config.FLIGHTS_DIRECTORY);
		
		try {
			if (!directory.exists() || directory.isFile()) {
				FileUtils.forceDirectoryCreation(directory);
			} else {
				File[] files = directory.listFiles(FilenameUtils.createFilter("json"));
				
				for (File file : files) {
					try {
						Flight flight = Flight.fromJsonObject(file);
						
						if (flight != null) {
							allFlights.put(file.getName(), flight);
							qualityManager.load(flight);
						}
					} catch (Exception exception) {
						LOGGER.warn("Failed to load flight file, deleting. (file = " + file.getName() + ")", exception);
						file.delete();
					}
				}
			}
		} catch (IOException exception) {
			LOGGER.error("Failed to initialize flight controller.", exception);
		}
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
		
		qualityManager.prepareCurrentFlight(currentFlight);
		exchangeManager.send(ExchangeManager.IDENTIFIER_FLIGHT_STARTING, new JsonObject());
	}
	
	public void stop() {
		checkActiveFlight();
		
		allFlights.put(currentFlight.getLocalFile().getName(), currentFlight.finish());
		
		exchangeManager.send(ExchangeManager.IDENTIFIER_FLIGHT_FINISHED, new JsonObject());
		
		currentFlight = null;
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
	
	public Flight getFlightByLocalFileName(String flightLocalFileName) {
		return allFlights.get(flightLocalFileName);
	}
	
	public Map<String, Flight> getAllFlight() {
		return allFlights;
	}
	
	@Override
	public void destroy() {
		LOGGER.info("Ending flight controller...");
		
		if (isFlightActive()) {
			try {
				currentFlight.rush().save();
			} catch (Exception exception) {
				LOGGER.error("Failed to properly save current flight.", exception);
			}
		}
		
		for (Flight flight : allFlights.values()) {
			try {
				flight.save();
			} catch (IOException exception) {
				LOGGER.warn("Failed to save flight. (file = " + flight.getLocalFile().getName() + ")", exception);
			}
		}
	}
	
	/** @return {@link FlightController}'s singleton. */
	public static FlightController getFlightController() {
		return Objects.requireNonNull(CONTROLLER, "Instance is null, did you call create() before ?");
	}
	
	/** @return Created {@link FlightController} singleton. */
	public static FlightController create() {
		return CONTROLLER = new FlightController();
	}
	
}