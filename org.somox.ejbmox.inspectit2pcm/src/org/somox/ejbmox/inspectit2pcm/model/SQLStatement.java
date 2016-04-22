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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sql == null) ? 0 : sql.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SQLStatement other = (SQLStatement) obj;
		if (sql == null) {
			if (other.sql != null)
				return false;
		} else if (!sql.equals(other.sql))
			return false;
		return true;
	}
	
	

}
