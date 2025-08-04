package dev.jgrove2.notes_backend.Controllers;

import dev.jgrove2.notes_backend.Utils.DatabaseConnectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseConnectionUtil databaseConnectionUtil;

    /**
     * Test database connection
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isConnected = databaseConnectionUtil.testConnection();
            response.put("status", isConnected ? "connected" : "disconnected");
            response.put("message", isConnected ? "Database connection successful" : "Database connection failed");
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection error: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get database information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = databaseConnectionUtil.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            response.put("databaseProductName", metaData.getDatabaseProductName());
            response.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            response.put("driverName", metaData.getDriverName());
            response.put("driverVersion", metaData.getDriverVersion());
            response.put("url", metaData.getURL());
            response.put("userName", metaData.getUserName());
            response.put("connectionInfo", databaseConnectionUtil.getConnectionInfo());
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Failed to get database info: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get database tables
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getDatabaseTables() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = databaseConnectionUtil.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" });

            java.util.List<String> tableNames = new java.util.ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            response.put("tables", tableNames);
            response.put("tableCount", tableNames.size());
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Failed to get database tables: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get database health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> response = new HashMap<>();

        boolean isHealthy = databaseConnectionUtil.testConnection();

        response.put("status", isHealthy ? "healthy" : "unhealthy");
        response.put("database", "neon-postgresql");
        response.put("connectionInfo", databaseConnectionUtil.getConnectionInfo());
        response.put("poolInfo", databaseConnectionUtil.getPoolInfo());
        response.put("timestamp", java.time.LocalDateTime.now());

        if (isHealthy) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Get connection pool statistics
     */
    @GetMapping("/pool-stats")
    public ResponseEntity<Map<String, Object>> getPoolStats() {
        Map<String, Object> response = new HashMap<>();

        response.put("poolInfo", databaseConnectionUtil.getPoolInfo());
        response.put("connectionInfo", databaseConnectionUtil.getConnectionInfo());
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}