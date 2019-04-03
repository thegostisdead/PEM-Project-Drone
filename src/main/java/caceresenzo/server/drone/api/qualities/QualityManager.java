package caceresenzo.server.drone.api.qualities;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import caceresenzo.server.drone.Config;
import caceresenzo.server.drone.api.flight.FlightController;
import caceresenzo.server.drone.api.flight.models.Flight;
import caceresenzo.server.drone.api.qualities.models.PhysicalQuality;
import caceresenzo.server.drone.api.qualities.models.ValueHolder;
import caceresenzo.server.drone.storage.SqliteStorage;
import caceresenzo.server.drone.utils.Initializable;

public class QualityManager implements Initializable {
	
	/* Instance */
	private static QualityManager MANAGER;
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(QualityManager.class);
	
	/* Managers */
	private QualityRegistry qualityRegistry;
	private FlightController flightController;
	
	/* Variables */
	private final Map<Flight, Map<PhysicalQuality, List<ValueHolder>>> cache;
	private SqliteStorage currentFlightSqliteStorage;
	
	/* Constructor */
	private QualityManager() {
		this.qualityRegistry = new QualityRegistry();
		
		this.cache = new HashMap<>();
	}
	
	@Override
	public void initialize() {
		flightController = FlightController.getFlightController();
	}
	
	/**
	 * Load to cache old data from a {@link Flight}.
	 * 
	 * @param flight
	 *            Target {@link Flight}.
	 */
	public void load(Flight flight) {
		SqliteStorage sqliteStorage = createSqliteStorage(flight);
		
		if (!sqliteStorage.connect()) {
			LOGGER.warn("Failed to connect to physical quality database of flight {}.", flight.getLocalFileName());
			return;
		}
		
		createDefaultTable(sqliteStorage);
		
		cache.put(flight, getHolderList(sqliteStorage));
		
		sqliteStorage.close();
	}
	
	public void prepareCurrentFlight(Flight flight) {
		if (currentFlightSqliteStorage != null && currentFlightSqliteStorage.isConnected()) {
			currentFlightSqliteStorage.close();
		}
		
		currentFlightSqliteStorage = createSqliteStorage(flight);
		if (!currentFlightSqliteStorage.connect()) {
			LOGGER.warn("Failed to open database for the new flight. This feature will be disabled.");
		} else {
			createDefaultTable(currentFlightSqliteStorage);
		}
		
		cache.put(flight, createEmptyHolderList());
	}
	
	/**
	 * Create a new {@link SqliteStorage} object with the configurable database path.
	 * 
	 * @param flight
	 *            Target {@link Flight}.
	 * @return A new {@link SqliteStorage}.
	 */
	public SqliteStorage createSqliteStorage(Flight flight) {
		return new SqliteStorage(new File(Config.API_PHYSICAL_QUALITIES_STORAGE_DATABASES_FOLDER, flight.getLocalFileName().concat(".db")).getAbsolutePath());
	}
	
	/**
	 * For all loaded physical qualities, do a "CREATE TABLE IF NOT EXISTS" to prepare database (in case of) to receive data.
	 * 
	 * @param sqliteStorage
	 *            Target connected {@link SqliteStorage}.
	 */
	public void createDefaultTable(SqliteStorage sqliteStorage) {
		for (PhysicalQuality physicalQuality : qualityRegistry.getLoaded()) {
			sqliteStorage.execute(String.format("CREATE TABLE IF NOT EXISTS `%s`( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `date` INTEGER NOT NULL, `content` INTEGER NOT NULL);", physicalQuality.getName()));
		}
	}
	
	/**
	 * Get {@link ValueHolder} from the database.<br>
	 * They will be sorted in a {@link Map} with the key as the {@link PhysicalQuality} they are referring to.
	 * 
	 * @param sqliteStorage
	 *            Target connected {@link SqliteStorage}.
	 * @return A {@link Map} filled with values from the database.
	 */
	public Map<PhysicalQuality, List<ValueHolder>> getHolderList(SqliteStorage sqliteStorage) {
		Map<PhysicalQuality, List<ValueHolder>> map = new HashMap<>();
		
		for (PhysicalQuality physicalQuality : qualityRegistry.getLoaded()) {
			List<ValueHolder> holders = new ArrayList<>();
			
			ResultSet resultSet = sqliteStorage.query(String.format("SELECT * FROM `%s` ORDER BY `id` DESC LIMIT %s;", physicalQuality.getName(), Config.API_PHYSICAL_QUALITIES_REQUEST_MAX_VALUE_SIZE));
			
			if (resultSet == null) {
				continue;
			}
			
			try {
				while (resultSet.next()) {
					long date = resultSet.getLong("date");
					String content = resultSet.getString("content");
					
					holders.add(new ValueHolder(date, content));
				}
				
				map.put(physicalQuality, holders);
			} catch (SQLException exception) {
				LOGGER.error("Failed to iterate over database selected content.", exception);
			}
		}
		
		return map;
	}
	
	public Map<PhysicalQuality, List<ValueHolder>> createEmptyHolderList() {
		Map<PhysicalQuality, List<ValueHolder>> map = new HashMap<>();
		
		qualityRegistry.getLoaded().forEach(physicalQuality -> map.put(physicalQuality, new ArrayList<>()));
		
		return map;
	}
	
	/**
	 * Insert the content of a {@link ValueHolder} to the database.
	 * 
	 * @param physicalQuality
	 *            Quality that is supposed to represent the value.
	 * @param valueHolder
	 *            Target {@link ValueHolder}.
	 */
	public void insertNewValues(PhysicalQuality physicalQuality, ValueHolder valueHolder) {
		if (currentFlightSqliteStorage != null && currentFlightSqliteStorage.isConnected()) {
			currentFlightSqliteStorage.execute(String.format("INSERT INTO `%s` (date, content) VALUES (%s, \"%s\");", physicalQuality.getName(), valueHolder.getDate(), valueHolder.getContent()));
			
			getCache(flightController.getCurrentFlight()).get(physicalQuality).add(valueHolder);
		}
	}
	
	public Map<PhysicalQuality, List<ValueHolder>> getCache(Flight flight) {
		return cache.get(flight);
	}
	
	public QualityRegistry getRegistry() {
		return qualityRegistry;
	}
	
	/** @return The created {@link QualityManager}'s singleton. */
	public static QualityManager create() {
		return MANAGER = new QualityManager();
	}
	
	/** @return Get the {@link QualityManager}'s singleton. */
	public static QualityManager getQualityManager() {
		return Objects.requireNonNull(MANAGER, "Instance is null, did you call create() before?");
	}
	
}