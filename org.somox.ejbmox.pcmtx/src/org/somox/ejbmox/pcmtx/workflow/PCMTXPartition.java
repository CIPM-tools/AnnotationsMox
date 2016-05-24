package org.somox.ejbmox.pcmtx.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.palladiosimulator.pcmtx.EntityType;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.pcmtx.PCMTXConfiguration;
import org.somox.ejbmox.pcmtx.model.ParsedSQLStatement;

public class PCMTXPartition {

    public static final String PARTITION_ID = "org.somox.ejbmox.pcmtx.partition";

    private PCMTXConfiguration configuration;

    private Map<SQLStatement, ParsedSQLStatement> parsedStatementsMap;

    private Set<EntityType> entityTypes;

    public PCMTXPartition(PCMTXConfiguration configuration) {
        this.configuration = configuration;
        parsedStatementsMap = new HashMap<>();
        entityTypes = new HashSet<>();
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

    public void addEntityType(EntityType entityType) {
        entityTypes.add(entityType);
    }

    public Set<EntityType> getEntityTypes() {
        return Collections.unmodifiableSet(entityTypes);
    }

}
