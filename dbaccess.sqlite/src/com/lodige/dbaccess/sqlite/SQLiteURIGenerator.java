/**
 * dbsqlite Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.dbaccess.sqlite;

import com.lodige.dbaccess.IjdbcURIGenerator;

/**
 * @author renken
 */
public class SQLiteURIGenerator implements IjdbcURIGenerator
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateURI(String dbhost, int dbport, String dbname)
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not instantiate sqlite driver.", e);
		}
		return "jdbc:sqlite:"+dbname+".sqlite";
	}

}
