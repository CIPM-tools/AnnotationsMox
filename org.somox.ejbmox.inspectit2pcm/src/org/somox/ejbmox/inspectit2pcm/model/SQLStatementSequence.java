package org.somox.ejbmox.inspectit2pcm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SQLStatementSequence {

	private List<SQLStatement> statements;

	public SQLStatementSequence() {
		statements = new ArrayList<>();
	}

	public void addAll(Collection<SQLStatement> statements) {
		this.statements.addAll(statements);
	}

	public void add(SQLStatement statement) {
		statements.add(statement);
	}

	public int size() {
		return statements.size();
	}

	public List<SQLStatement> getSequence() {
		return Collections.unmodifiableList(statements);
	}

}
