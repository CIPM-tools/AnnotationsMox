package org.annotationsmox.pcmtx.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.annotationsmox.pcmtx.PCMTXConfiguration;
import org.annotationsmox.pcmtx.model.ParsedSQLStatement;
import org.palladiosimulator.pcmtx.EntityType;
import org.palladiosimulator.pcmtx.Table;

public class PCMTXPartition {

    public static final String PARTITION_ID = "org.somox.ejbmox.pcmtx.partition";

    private PCMTXConfiguration configuration;

    private Map<SQLStatement, ParsedSQLStatement> parsedStatementsMap;

    private Map<String, EntityType> tableNameToEntityTypeMap;

    private Set<EntityType> entityTypes;

    private Set<Table> tables;

    public PCMTXPartition(PCMTXConfiguration configuration) {
        this.configuration = configuration;
        parsedStatementsMap = new HashMap<>();
        tableNameToEntityTypeMap = new HashMap<>();
        entityTypes = new HashSet<>();
        tables = new HashSet<>();
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

    public void addTableNameToEntityTypeMapping(String tableName, EntityType entityType) {
        tableNameToEntityTypeMap.put(tableName, entityType);
    }

    public Map<String, EntityType> getTableNameToEntityTypeMap() {
        return Collections.unmodifiableMap(tableNameToEntityTypeMap);
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public Set<Table> getTables() {
        return Collections.unmodifiableSet(tables);
    }

}
