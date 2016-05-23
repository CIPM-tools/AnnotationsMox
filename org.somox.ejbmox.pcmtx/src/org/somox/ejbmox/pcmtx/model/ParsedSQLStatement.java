package org.somox.ejbmox.pcmtx.model;

import java.util.List;

public class ParsedSQLStatement {

    private StatementType type;
    
    private List<String> tableNames;

    public ParsedSQLStatement(StatementType type, List<String> tableNames) {
        this.type = type;
        this.tableNames = tableNames;
    }

    public StatementType getType() {
        return type;
    }

    public List<String> getTableNames() {
        return tableNames;
    }
    
}
