/**
 * dboracle Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.dbaccess.oracle;

import com.lodige.dbaccess.IjdbcURIGenerator;

/**
 * URI generator for oracles thin client.
 * jdbc:oracle:thin:@//<host>:<port>/<service_name> 
 * @author renken
 */
public class OracleThinURIGenerator implements IjdbcURIGenerator
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateURI(String dbhost, int dbport, String dbname)
	{
		return "jdbc:oracle:thin:@//"+dbhost+(dbport >= 0 ? ":" + dbport : "")+"/"+dbname;
	}

}
