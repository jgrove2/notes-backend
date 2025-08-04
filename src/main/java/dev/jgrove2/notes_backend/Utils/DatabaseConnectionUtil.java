package dev.jgrove2.notes_backend.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@Component
public class DatabaseConnectionUtil {

    private static final Logger logger = Logger.getLogger(DatabaseConnectionUtil.class.getName());

    @Autowired
    private DataSource dataSource;

    /**
     * Get a connection from the connection pool
     * 
     * @return Connection object from the pool
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            logger.info("Successfully obtained connection from pool");
            return connection;
        } catch (SQLException e) {
            logger.severe("Failed to get connection from pool: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test the database connection using the pool
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            logger.severe("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get database connection info (without sensitive data)
     * 
     * @return Database connection info
     */
    public String getConnectionInfo() {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                String jdbcUrl = hikariDS.getJdbcUrl();

                // Extract host and database name from URL for logging
                if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
                    try {
                        String[] parts = jdbcUrl.split("/");
                        if (parts.length >= 2) {
                            String hostPart = parts[2]; // jdbc:postgresql://host:port
                            String dbName = parts[parts.length - 1].split("\\?")[0];
                            return "Host: " + hostPart + ", Database: " + dbName + " (Pooled)";
                        }
                    } catch (Exception e) {
                        // If parsing fails, return a safe version
                        return "Database URL configured (Pooled)";
                    }
                }
            }
            return "Database URL configured (Pooled)";
        } catch (Exception e) {
            return "Database URL not configured (Pooled)";
        }
    }

    /**
     * Close a database connection safely (returns it to the pool)
     * 
     * @param connection Connection to close
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Connection returned to pool");
            } catch (SQLException e) {
                logger.warning("Error returning connection to pool: " + e.getMessage());
            }
        }
    }

    /**
     * Get connection pool statistics
     * 
     * @return Connection pool info
     */
    public String getPoolInfo() {
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                return String.format("Pool: %s, Active: %d, Idle: %d, Total: %d",
                        hikariDS.getPoolName(),
                        hikariDS.getHikariPoolMXBean().getActiveConnections(),
                        hikariDS.getHikariPoolMXBean().getIdleConnections(),
                        hikariDS.getHikariPoolMXBean().getTotalConnections());
            }
            return "Connection pool info not available";
        } catch (Exception e) {
            return "Error getting pool info: " + e.getMessage();
        }
    }
}