# Neon Database Setup Guide

This guide explains how to configure your Spring Boot application to connect to a Neon PostgreSQL database.

## 🚀 Quick Setup

### 1. Get Your Neon Database URL

1. Log into your [Neon Console](https://console.neon.tech/)
2. Select your project
3. Go to the **Connection Details** tab
4. Copy the connection string

### 2. Configure Environment Variables

Update your `.env` file with your Neon database credentials:

```bash
# Neon Database Configuration
SPRING_DATASOURCE_URL=postgresql://username:password@host:5432/database
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
```

### 3. Example Neon Connection String

A typical Neon connection string looks like:

```
postgresql://username:password@ep-cool-name-123456.us-east-2.aws.neon.tech:5432/neondb
```

## 🔧 Configuration Details

### Environment Variables

| Variable                     | Description                  | Example                               |
| ---------------------------- | ---------------------------- | ------------------------------------- |
| `SPRING_DATASOURCE_URL`      | Full database connection URL | `postgresql://user:pass@host:5432/db` |
| `SPRING_DATASOURCE_USERNAME` | Database username            | `your-username`                       |
| `SPRING_DATASOURCE_PASSWORD` | Database password            | `your-password`                       |

### Application Properties

The application automatically configures these properties from environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

## 🧪 Testing the Connection

### API Endpoints

Once your application is running, you can test the database connection:

1. **Test Connection**: `GET /api/database/test`
2. **Database Info**: `GET /api/database/info`
3. **List Tables**: `GET /api/database/tables`
4. **Health Check**: `GET /api/database/health`

### Example Responses

**Successful Connection:**

```json
{
  "status": "connected",
  "message": "Database connection successful",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Database Info:**

```json
{
  "databaseProductName": "PostgreSQL",
  "databaseProductVersion": "15.4",
  "driverName": "PostgreSQL JDBC Driver",
  "driverVersion": "42.6.0",
  "connectionInfo": "Host: ep-cool-name-123456.us-east-2.aws.neon.tech:5432, Database: neondb"
}
```

## 🔒 Security Features

### SSL Configuration

The utility automatically configures SSL for Neon:

- `sslmode=require`
- Automatic SSL certificate handling
- Secure connection by default

### Connection Properties

```java
Properties props = new Properties();
props.setProperty("sslmode", "require");
props.setProperty("autoCommit", "false");
props.setProperty("readOnly", "false");
```

## 🛠️ Utility Functions

### DatabaseConnectionUtil

The `DatabaseConnectionUtil` class provides:

- **`getConnection()`**: Get a database connection
- **`testConnection()`**: Test if connection is working
- **`getConnectionInfo()`**: Get connection details (safe for logging)
- **`closeConnection()`**: Safely close connections

### Usage Example

```java
@Autowired
private DatabaseConnectionUtil dbUtil;

public void example() {
    try (Connection conn = dbUtil.getConnection()) {
        // Use the connection
        System.out.println("Connected to: " + dbUtil.getConnectionInfo());
    } catch (SQLException e) {
        // Handle connection error
    }
}
```

## 🚨 Troubleshooting

### Common Issues

1. **Connection Refused**

   - Check if your Neon database is active
   - Verify the connection string format
   - Ensure your IP is whitelisted (if applicable)

2. **Authentication Failed**

   - Verify username and password
   - Check if credentials are correctly set in `.env`

3. **SSL Issues**
   - Neon requires SSL connections
   - The utility automatically configures SSL

### Debug Steps

1. Check your `.env` file configuration
2. Test connection: `GET /api/database/test`
3. View database info: `GET /api/database/info`
4. Check application logs for detailed error messages

## 📊 Monitoring

### Health Check Endpoint

Use `/api/database/health` for monitoring:

- Returns 200 if database is healthy
- Returns 503 if database is unavailable
- Includes connection info and timestamp

### Logging

The utility logs connection events:

- Connection success/failure
- Connection closure
- Error details (without sensitive data)

## 🔄 Environment-Specific Configuration

### Development

```bash
SPRING_DATASOURCE_URL=postgresql://dev-user:dev-pass@dev-host:5432/dev-db
```

### Production

```bash
SPRING_DATASOURCE_URL=postgresql://prod-user:prod-pass@prod-host:5432/prod-db
```

### Testing

```bash
SPRING_DATASOURCE_URL=postgresql://test-user:test-pass@test-host:5432/test-db
```

## 📝 Notes

- The utility automatically handles connection pooling through Spring Boot
- SSL is required for Neon connections
- Connection timeouts are set to 5 seconds for health checks
- Sensitive data is not logged for security
