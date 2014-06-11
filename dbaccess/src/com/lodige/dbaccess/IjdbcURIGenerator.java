/**
 * dbaccess Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.dbaccess;

/**
 * @author renken
 *
 */
public interface IjdbcURIGenerator
{

	public static final String EXTPOINT_JDBCURIGEN = "com.lodige.dbaccess.jdbcurigen";


	public String generateURI(String dbhost, int dbport, String dbname);
}
