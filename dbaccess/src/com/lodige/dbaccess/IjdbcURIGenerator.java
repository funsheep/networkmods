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

	public static final String EP_JDBCURIGEN = "com.lodige.dbaccess.jdbcurigen";


	public String generateURI(String dbhost, int dbpost, String dbname);
}
