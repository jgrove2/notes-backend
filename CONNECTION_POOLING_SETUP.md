# Connection Pooling Setup Guide

This guide explains the connection pooling configuration using HikariCP for your Spring Boot application with Neon PostgreSQL.

## 🚀 Overview

Connection pooling improves performance by:

- **Reusing connections** instead of creating new ones for each request
- **Reducing connection overhead** and database load
- **Improving response times** for database operations
- **Managing connection lifecycle** automatically

## 🔧 Configuration

### Environment Variables

The following environment variables control the connection pool:

| Variable                                            | Default | Description                            |
| --------------------------------------------------- | ------- | -------------------------------------- |
| `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT`       | 30000   | Max time to wait for a connection (ms) |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`        | 20      | Maximum number of connections in pool  |
| `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE`             | 5       | Minimum idle connections to maintain   |
| `SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT`             | 300000  | Max time connections can be idle (ms)  |
| `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME`             | 1200000 | Max lifetime of connections (ms)       |
| `SPRING_DATASOURCE_HIKARI_LEAK_DETECTION_THRESHOLD` | 60000   | Leak detection threshold (ms)          |

### Application Properties

```properties
# Connection Pooling Configuration (HikariCP)
spring.datasource.hikari.connection-timeout=${SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT:30000}
spring.datasource.hikari.maximum-pool-size=${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:20}
spring.datasource.hikari.minimum-idle=${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:5}
spring.datasource.hikari.idle-timeout=${SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT:300000}
spring.datasource.hikari.max-lifetime=${SPRING_DATASOURCE_HIKARI_MAX_LIFETIME:1200000}
spring.datasource.hikari.leak-detection-threshold=${SPRING_DATASOURCE_HIKARI_LEAK_DETECTION_THRESHOLD:60000}
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000
```

## 🏗️ Architecture

### Components

1. **`DatabaseConfig`** - Configures HikariCP DataSource
2. **`DatabaseConnectionUtil`** - Uses pooled connections
3. **`DatabaseController`** - Monitors pool statistics

### Connection Flow

```
Request → DatabaseConnectionUtil → HikariCP Pool → Neon Database
```

## 📊 Monitoring

### API Endpoints

| Endpoint                       | Description                 |
| ------------------------------ | --------------------------- |
| `GET /api/database/health`     | Health check with pool info |
| `GET /api/database/pool-stats` | Detailed pool statistics    |

### Example Responses

**Health Check:**

```json
{
  "status": "healthy",
  "database": "neon-postgresql",
  "connectionInfo": "Host: ep-cool-name-123456.us-east-2.aws.neon.tech:5432, Database: neondb (Pooled)",
  "poolInfo": "Pool: NotesBackendHikariCP, Active: 2, Idle: 8, Total: 10",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Pool Statistics:**

```json
{
  "poolInfo": "Pool: NotesBackendHikariCP, Active: 2, Idle: 8, Total: 10",
  "connectionInfo": "Host: ep-cool-name-123456.us-east-2.aws.neon.tech:5432, Database: neondb (Pooled)",
  "timestamp": "2024-01-15T10:30:00"
}
```

## ⚙️ Configuration Details

### HikariCP Settings

- **Connection Timeout**: 30 seconds to get a connection
- **Maximum Pool Size**: 20 concurrent connections
- **Minimum Idle**: 5 connections always available
- **Idle Timeout**: 5 minutes before closing idle connections
- **Max Lifetime**: 20 minutes maximum connection lifetime
- **Leak Detection**: 60 seconds to detect connection leaks

### Neon-Specific Optimizations

```java
// SSL Configuration
config.addDataSourceProperty("sslmode", "require");

// Performance Optimizations
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
config.addDataSourceProperty("useServerPrepStmts", "true");
```

## 🚨 Performance Considerations

### Pool Sizing Guidelines

| Application Type | Pool Size | Reasoning                         |
| ---------------- | --------- | --------------------------------- |
| **Development**  | 5-10      | Light load, fewer users           |
| **Production**   | 10-20     | Moderate load                     |
| **High Traffic** | 20-50     | Heavy load, many concurrent users |

### Environment-Specific Settings

**Development:**

```bash
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=2
```

**Production:**

```bash
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
```

**High Traffic:**

```bash
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=50
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=10
```

## 🔍 Troubleshooting

### Common Issues

1. **Connection Timeout**

   - Increase `connection-timeout`
   - Check database availability
   - Verify network connectivity

2. **Pool Exhaustion**

   - Increase `maximum-pool-size`
   - Check for connection leaks
   - Optimize query performance

3. **High Memory Usage**
   - Reduce `maximum-pool-size`
   - Monitor connection usage
   - Check for connection leaks

### Debug Steps

1. **Check Pool Statistics:**

   ```
   GET /api/database/pool-stats
   ```

2. **Monitor Health:**

   ```
   GET /api/database/health
   ```

3. **Review Logs:**
   - Look for connection pool messages
   - Check for timeout errors
   - Monitor leak detection warnings

## 📈 Best Practices

### Connection Management

- **Always close connections** in try-with-resources
- **Use connection pooling** for all database operations
- **Monitor pool statistics** regularly
- **Set appropriate timeouts** for your use case

### Performance Optimization

- **Cache prepared statements** (enabled by default)
- **Use connection validation** (enabled by default)
- **Monitor connection leaks** (detection enabled)
- **Tune pool size** based on load testing

### Security

- **SSL connections** required for Neon
- **Connection validation** prevents stale connections
- **Leak detection** prevents resource exhaustion
- **Proper error handling** for connection failures

## 🔄 Migration from Manual Connections

### Before (Manual Connections)

```java
// Old way - creates new connection each time
Connection conn = DriverManager.getConnection(url, props);
// ... use connection
conn.close();
```

### After (Connection Pooling)

```java
// New way - uses pooled connections
@Autowired
private DatabaseConnectionUtil dbUtil;

public void example() {
    try (Connection conn = dbUtil.getConnection()) {
        // ... use connection
        // Automatically returned to pool when closed
    }
}
```

## 📝 Notes

- **HikariCP** is the default and fastest connection pool for Spring Boot
- **Automatic configuration** via Spring Boot auto-configuration
- **Connection validation** ensures connections are healthy
- **Leak detection** helps identify connection leaks
- **Performance monitoring** available through JMX and API endpoints
