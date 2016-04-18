package org.somox.ejbmox.inspectit2pcm.model;

/**
 * 
 * @author Patrice Bouillet (parts of this class have been copied from
 *         InspectIT)
 * @author Philipp Merkle
 *
 */
public class SQLStatement extends TimerData {

	/**
	 * The SQL-String of the Statement.
	 */
	private String sql;

	/**
	 * The URL that the connection uses.
	 */
	private String databaseUrl;

	public SQLStatement(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	@Override
	public String toString() {
		return sql;
	}

}
