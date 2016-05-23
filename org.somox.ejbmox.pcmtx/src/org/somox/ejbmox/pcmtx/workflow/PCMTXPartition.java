package org.somox.ejbmox.pcmtx.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.pcmtx.PCMTXConfiguration;
import org.somox.ejbmox.pcmtx.model.ParsedSQLStatement;

public class PCMTXPartition {

    public static final String PARTITION_ID = "org.somox.ejbmox.pcmtx.partition";
    
    private PCMTXConfiguration configuration;
    
    private Map<SQLStatement, ParsedSQLStatement> parsedStatementsMap;
    
    public PCMTXPartition(PCMTXConfiguration configuration) {
        this.configuration = configuration;
        parsedStatementsMap = new HashMap<>();
    }
    
    public PCMTXConfiguration getConfiguration() {
        return configuration;
    }
    
    public void addParsedStatement(SQLStatement stmt, ParsedSQLStatement parsedStmt) {
        parsedStatementsMap.put(stmt, parsedStmt);
    }
    
    public Map<SQLStatement, ParsedSQLStatement> getParsedStatementsMap() {
        return Collections.unmodifiableMap(parsedStatementsMap);
    }
    
}
