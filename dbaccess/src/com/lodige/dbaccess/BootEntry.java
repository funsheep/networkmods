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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

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

		IjdbcURIGenerator generator = ExtensionRegistry.getService(IjdbcURIGenerator.EXTPOINT_JDBCURIGEN);
		if (generator == null)
			throw new PlatformException("Could not instantiate IjdbcURIGenerator.");
		
		BasicDataSource pool = new BasicDataSource();
		pool.setUsername(dbuser);
		pool.setPassword(dbpswd);
		pool.setUrl(generator.generateURI(dbhost, dbport, dbname));
		ExtensionRegistry.registerSingleton(DataSource.class.getName(), EXTPOINT_DBCP, pool, null);
		
		LOGGER.info("Started connection pool with URI {}", pool.getUrl());
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
