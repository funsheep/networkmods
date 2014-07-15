/**
 * dbaccess Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.dbaccess;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * @author renken
 *
 */
public class BootEntry implements IBootEntry
{

	public static final String EXTPOINT_DBCP = "com.lodige.dbaccess.pool";

	private static final Logger LOGGER = Logger.getLogger();


	private static final String O_DBHOST = "dbhost";
	private static final String O_DBPORT = "dbport";
	private static final String O_DBNAME = "dbname";
	private static final String O_DBUSER = "dbuser";
	private static final String O_DBPSWD = "dbpswd";

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		String dbhost = Platform.getOptionValue(O_DBHOST, null);
		int dbport = Platform.getOptionValue(O_DBPORT, -1);
		String dbname = Platform.getOptionValue(O_DBNAME, null);
		String dbuser = Platform.getOptionValue(O_DBUSER, null);
		String dbpswd = Platform.getOptionValue(O_DBPSWD, null);

		Extension genex = ExtensionRegistry.getExtension(IjdbcURIGenerator.EXTPOINT_JDBCURIGEN);
		if (genex == null)
			throw new PlatformException("Could not find IjdbcURIGenerator extension.");

		IjdbcURIGenerator generator;
		try
		{
			generator = genex.getService();
		}
		catch (ServiceInstantiationException e1)
		{
			throw new PlatformException("Could not instantiate JDBC driver.", e1);
		}

		BasicDataSource pool = new BasicDataSource();
		pool.setUsername(dbuser);
		pool.setPassword(dbpswd);
		pool.setUrl(generator.generateURI(dbhost, dbport, dbname));
		this.initPool(pool, e, genex);
		ExtensionRegistry.registerSingleton(DataSource.class.getName(), EXTPOINT_DBCP, pool, null);
		
		LOGGER.info("Started connection pool with URI {}", pool.getUrl());
	}
	
	private void initPool(BasicDataSource pool, Extension dbaccess, Extension generator)
	{
		String validQuery = generator.getProperty("validationQuery");
		if (validQuery != null)
			pool.setValidationQuery(validQuery);
		pool.setTestOnBorrow(dbaccess.getProperty("testOnBorrow", true));
		pool.setTestWhileIdle(dbaccess.getProperty("validationInterval", -1) != -1);
		pool.setTimeBetweenEvictionRunsMillis(dbaccess.getProperty("timeBetweenEvictionRunsMillis", -1));
		pool.setMinEvictableIdleTimeMillis(dbaccess.getProperty("minEvictableIdleTimeMillis", -1));
		pool.setRemoveAbandonedOnMaintenance(dbaccess.getProperty("removeAbandoned", true));
		pool.setRemoveAbandonedTimeout(dbaccess.getProperty("removeAbandonedTimeout", 300));
		pool.setValidationQueryTimeout(dbaccess.getProperty("validationQueryTimeout", 10));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws PlatformException
	{
		BasicDataSource pool = ExtensionRegistry.getService(EXTPOINT_DBCP);
		if (pool != null)
		{
			try
			{
				pool.close();
			}
			catch (SQLException e)
			{
				throw new PlatformException("Could not properly shutdown db connection pool.", e);
			}
		}
	}

}
