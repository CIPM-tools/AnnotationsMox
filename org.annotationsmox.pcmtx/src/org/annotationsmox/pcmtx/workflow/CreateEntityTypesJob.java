package org.annotationsmox.pcmtx.workflow;

import java.util.HashSet;
import java.util.Set;

import org.annotationsmox.pcmtx.model.ParsedSQLStatement;
import org.annotationsmox.util.EMFHelper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcmtx.EntityType;
import org.palladiosimulator.pcmtx.api.EntityTypesAPI;
import org.somox.configuration.FileLocationConfiguration;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CreateEntityTypesJob extends AbstractPCMTXJob {

    private static final String FILENAME_ENTITY_TYPES_REPOSITORY = "entitytypes.resourcetype";

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // create repository
        ResourceRepository repository = EntityTypesAPI.createEmptyRepository();

        // calculate set of distinct table names
        PCMTXPartition partition = getPCMTXPartition();
        Set<String> tableNames = new HashSet<>();
        for (ParsedSQLStatement stmt : partition.getParsedStatementsMap().values()) {
            tableNames.addAll(stmt.getTableNames());
        }

        // create entity types from table names, assuming a 1:1 relationship
        for (String tableName : tableNames) {
            String entityName = tableNameToCamelCase(tableName);
            EntityType entityType = EntityTypesAPI.createEntityType(repository, entityName);
            getPCMTXPartition().addEntityType(entityType);
            getPCMTXPartition().addTableNameToEntityTypeMapping(tableName, entityType);
        }

        // save to XMI file
        FileLocationConfiguration locations = getEJBMoXConfiguration().getFileLocations();
        EMFHelper.createResourceAndSave(repository, FILENAME_ENTITY_TYPES_REPOSITORY, locations, logger);
    }

    public String tableNameToCamelCase(String tableName) {
        String[] segments = tableName.split("_|\\."); // underscore or dot
        for (int i = 0; i < segments.length; i++) {
            String s = segments[i];
            if (s.length() <= 1) {
                continue;
            }
            String first = s.substring(0, 1);
            String remainder = s.substring(1, s.length());
            segments[i] = first.toUpperCase() + remainder.toLowerCase();
        }
        return String.join("", segments);
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Create Entity Types";
    }

}
