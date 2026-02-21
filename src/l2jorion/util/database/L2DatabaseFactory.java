package l2jorion.util.database;

import java.sql.Connection;
import java.sql.SQLException;

import l2jorion.logger.Logger;
import l2jorion.logger.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import l2jorion.Config;
import l2jorion.game.thread.ThreadPoolManager;

public class L2DatabaseFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(L2DatabaseFactory.class);
	
	/** Maximum number of retry attempts before giving up on obtaining a connection. */
	private static final int MAX_CONNECTION_RETRIES = 10;
	
	private static volatile L2DatabaseFactory _instance;
	private static HikariDataSource dataSource;
	
	static
	{
		try
		{
			// Suppress HikariCP internal slf4j-simple logs (they use a different format)
			System.setProperty("org.slf4j.simpleLogger.log.com.zaxxer.hikari", "error");
			
			final HikariConfig config = buildBaseConfig();
			
			// =========================================================
			// First attempt: SSL / TLS enabled (verify-none)
			// =========================================================
			applySSLProperties(config, true);
			
			LOGGER.info("Initializing HikariCP pool (max={}, minIdle={})...", Config.DATABASE_MAX_CONNECTIONS, Math.max(2, Config.DATABASE_MAX_CONNECTIONS / 2));
			
			try
			{
				dataSource = new HikariDataSource(config);
				LOGGER.info("Connected with SSL/TLS enabled.");
			}
			catch (Exception sslEx)
			{
				if (isSSLRelatedFailure(sslEx))
				{
					LOGGER.warn("SSL/TLS not supported. Falling back to non-SSL...");
					
					// =========================================================
					// Fallback: rebuild config without SSL
					// =========================================================
					final HikariConfig fallbackConfig = buildBaseConfig();
					applySSLProperties(fallbackConfig, false);
					
					dataSource = new HikariDataSource(fallbackConfig);
					LOGGER.info("Connected without SSL (plain connection).");
					LOGGER.warn("WARNING: Database traffic is NOT encrypted.");
				}
				else
				{
					throw sslEx; // Not an SSL issue — propagate
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to create HikariCP DataSource: {}", e.getMessage(), e);
		}
	}
	
	private static HikariConfig buildBaseConfig()
	{
		final HikariConfig config = new HikariConfig();
		
		// =========================================================
		// Basic connection settings
		// =========================================================
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		config.setJdbcUrl(sanitizeJdbcUrl(Config.DATABASE_URL));
		config.setUsername(Config.DATABASE_LOGIN);
		config.setPassword(Config.DATABASE_PASSWORD);
		
		// =========================================================
		// HikariCP pool settings (high performance)
		// =========================================================
		config.setPoolName("L2J-DatabasePool");
		config.setMaximumPoolSize(Config.DATABASE_MAX_CONNECTIONS);
		config.setMinimumIdle(Math.max(2, Config.DATABASE_MAX_CONNECTIONS / 2));
		config.setIdleTimeout(300_000); // 5 min idle before eviction
		config.setMaxLifetime(1_800_000); // 30 min max connection lifetime
		config.setConnectionTimeout(30_000); // 30s wait for a connection
		config.setValidationTimeout(5_000); // 5s to validate a connection
		config.setKeepaliveTime(60_000); // 60s keep-alive ping
		config.setConnectionTestQuery("SELECT 1");
		
		// =========================================================
		// Leak detection
		// =========================================================
		config.setLeakDetectionThreshold(60_000); // 60s
		
		// =========================================================
		// MariaDB performance data source properties
		// =========================================================
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		config.addDataSourceProperty("rewriteBatchedStatements", "true");
		config.addDataSourceProperty("useLocalSessionState", "true");
		config.addDataSourceProperty("cacheResultSetMetadata", "true");
		config.addDataSourceProperty("cacheServerConfiguration", "true");
		config.addDataSourceProperty("maintainTimeStats", "false");
		config.addDataSourceProperty("useUnicode", "true");
		config.addDataSourceProperty("characterEncoding", "utf8");
		config.addDataSourceProperty("socketTimeout", "30000");
		
		return config;
	}
	
	/**
	 * Apply or disable SSL-related JDBC properties.
	 * @param config HikariConfig to modify
	 * @param enable true = force SSL, false = disable SSL
	 */
	private static void applySSLProperties(HikariConfig config, boolean enable)
	{
		if (enable)
		{
			config.addDataSourceProperty("sslMode", "verify-none");
		}
		else
		{
			config.addDataSourceProperty("sslMode", "disable");
		}
	}
	
	private static String sanitizeJdbcUrl(String url)
	{
		if (url == null || url.isEmpty())
		{
			return url;
		}
		
		// Remove SSL-related parameters that could override our HikariConfig settings
		url = url.replaceAll("[&?](useSsl|useSSL|requireSSL|verifyServerCertificate|sslMode)=[^&]*", "");
		
		// Fix url if it ends with ? or & after parameter removal
		url = url.replaceAll("[?&]$", "");
		
		// Fix double && or ?& after removal
		url = url.replace("&&", "&").replace("?&", "?");
		
		LOGGER.debug("Sanitized JDBC URL: {}", url);
		return url;
	}
	
	private static boolean isSSLRelatedFailure(Throwable e)
	{
		while (e != null)
		{
			String msg = e.getMessage();
			if (msg != null)
			{
				String lower = msg.toLowerCase();
				if (lower.contains("ssl") || lower.contains("tls") || lower.contains("certificate") || lower.contains("handshake") || lower.contains("encrypted"))
				{
					return true;
				}
			}
			e = e.getCause();
		}
		return false;
	}
	
	public static void init()
	{
		if (dataSource == null || dataSource.isClosed())
		{
			LOGGER.error("DataSource is NOT available. Server cannot start.");
			return;
		}
		
		try (Connection con = dataSource.getConnection())
		{
			LOGGER.info("Connection test OK. Pool is ready. (valid={})", con.isValid(5));
		}
		catch (Exception e)
		{
			LOGGER.error("Problem on initialize.", e);
		}
	}
	
	public void shutdown()
	{
		try
		{
			if (dataSource != null && !dataSource.isClosed())
			{
				dataSource.close();
				LOGGER.info("HikariCP connection pool closed.");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error closing connection pool.", e);
		}
	}
	
	public static HikariDataSource getDataSource()
	{
		return dataSource;
	}
	
	public final String safetyString(final String... whatToCheck)
	{
		final char braceLeft = '`';
		final char braceRight = '`';
		
		int length = 0;
		for (final String word : whatToCheck)
		{
			length += word.length() + 4;
		}
		
		final StringBuilder sbResult = new StringBuilder(length);
		
		for (final String word : whatToCheck)
		{
			if (sbResult.length() > 0)
			{
				sbResult.append(", ");
			}
			
			sbResult.append(braceLeft);
			sbResult.append(word);
			sbResult.append(braceRight);
		}
		
		return sbResult.toString();
	}
	
	public static L2DatabaseFactory getInstance()
	{
		L2DatabaseFactory local = _instance;
		if (local == null)
		{
			synchronized (L2DatabaseFactory.class)
			{
				local = _instance;
				if (local == null)
				{
					local = new L2DatabaseFactory();
					_instance = local;
				}
			}
		}
		return local;
	}
	
	public Connection getConnection()
	{
		Connection con = null;
		int retries = 0;
		while (con == null)
		{
			try
			{
				con = dataSource.getConnection();
			}
			catch (final SQLException e)
			{
				retries++;
				if (retries >= MAX_CONNECTION_RETRIES)
				{
					LOGGER.error("Failed to get connection after {} retries. Active={}, Idle={}, Waiting={}", retries, dataSource.getHikariPoolMXBean() != null ? dataSource.getHikariPoolMXBean().getActiveConnections() : -1, dataSource.getHikariPoolMXBean() != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : -1, dataSource.getHikariPoolMXBean() != null ? dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection() : -1);
					throw new RuntimeException("L2DatabaseFactory: Unable to obtain database connection after " + retries + " retries.", e);
				}
				LOGGER.warn("Connection attempt {}/{} failed: {}", retries, MAX_CONNECTION_RETRIES, e.getMessage());
				try
				{
					Thread.sleep(100 * retries); // Progressive back-off: 100ms, 200ms, ...
				}
				catch (InterruptedException ignored)
				{
					Thread.currentThread().interrupt();
					throw new RuntimeException("L2DatabaseFactory: Thread interrupted while waiting for connection.", e);
				}
			}
		}
		return con;
	}
	
	public Connection getConnection(boolean checkclose)
	{
		Connection con = getConnection();
		if (checkclose && Config.DATABASE_CONNECTION_TIMEOUT > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.DATABASE_CONNECTION_TIMEOUT);
		}
		return con;
	}
}