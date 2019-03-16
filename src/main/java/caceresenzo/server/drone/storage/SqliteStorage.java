package caceresenzo.server.drone.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqliteStorage {
	
	/* Static */
	private static Logger LOGGER = LoggerFactory.getLogger(SqliteStorage.class);
	
	/* Variables */
	private final String path;
	private Connection connection;
	private boolean connected;
	
	/* Constructor */
	public SqliteStorage(String path) {
		this.path = path;
	}
	
	/**
	 * Initialize the connection with the SQLite database.
	 * 
	 * @return Weather or not the connection has been successful.
	 * @throws RuntimeException
	 *             If anything goes wrong.
	 */
	public synchronized boolean connect() {
		try {
			connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
			
			LOGGER.info("Connected to database. (path = {})", path);
		} catch (SQLException exception) {
			LOGGER.error("Failed to connect to the database.", exception);
			return connected = false;
		}
		
		return connected = true;
	}
	
	/**
	 * Execute some SQL.
	 * 
	 * @param sql
	 *            Target SQL to execute.
	 * @return If the request has successfully be done.
	 * @throws IllegalStateException
	 *             If the SQLite is not connected yet.
	 * @see #checkConnection()
	 */
	public boolean execute(String sql) {
		checkConnection();
		
		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
			
			return true;
		} catch (SQLException exception) {
			LOGGER.error("Failed to execute query.", exception);
		}
		
		return false;
	}
	
	/**
	 * Get the result of the SQL query.
	 * 
	 * @param sql
	 *            Target SQL.
	 * @return A {@link ResultSet} to iterate containing all the result or null if failed.
	 * @throws IllegalStateException
	 *             If the SQLite is not connected yet.
	 * @see #checkConnection()
	 */
	public ResultSet query(String sql) {
		checkConnection();
		
		try {
			return connection.createStatement().executeQuery(sql);
		} catch (SQLException exception) {
			LOGGER.error("Failed to execute query.", exception);
		}
		
		return null;
	}
	
	/** @return Weather or not the sqlite is connected with this object. */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * @throws IllegalStateException
	 *             If the SQLite is not connected yet!
	 */
	private void checkConnection() {
		if (!connected) {
			throw new IllegalStateException("SQLite is not connected.");
		}
	}
	
	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException exception) {
			LOGGER.error("Failed to close properly the connection with the SQLite.", exception);
		}
	}
	
}
