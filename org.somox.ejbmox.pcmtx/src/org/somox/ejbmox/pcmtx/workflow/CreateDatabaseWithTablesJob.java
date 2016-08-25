package org.somox.ejbmox.pcmtx.workflow;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.pcmtx.DataRepository;
import org.palladiosimulator.pcmtx.Database;
import org.palladiosimulator.pcmtx.EntityType;
import org.palladiosimulator.pcmtx.PcmtxFactory;
import org.palladiosimulator.pcmtx.Table;
import org.somox.configuration.FileLocationConfiguration;
import org.somox.ejbmox.util.EMFHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CreateDatabaseWithTablesJob extends AbstractPCMTXJob {

    private static final String FILENAME_DATA_REPOSITORY = "datarepository.pcmtx";

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // create repository
        DataRepository repository = PcmtxFactory.eINSTANCE.createDataRepository();

        // create database
        Database database = PcmtxFactory.eINSTANCE.createDatabase();
        database.setEntityName("Default Database"); // TODO
        repository.getDatabases().add(database);

        // create table for each entity type, assuming 1:1 relationship
        for (EntityType entityType : getPCMTXPartition().getEntityTypes()) {
            Table table = PcmtxFactory.eINSTANCE.createTable();
            table.setEntityName(entityType.getEntityName()); // TODO "Table" suffix?
            table.getTypes().add(entityType);
            table.setDatabase(database);
            table.setRows(0); // TODO
            repository.getTable().add(table); // TODO getTable --> getTables
            
            // add table to blackboard partition
            getPCMTXPartition().addTable(table);
        }

        // save to XMI file
        FileLocationConfiguration locations = getEJBMoXConfiguration().getFileLocations();
        EMFHelper.createResourceAndSave(repository, FILENAME_DATA_REPOSITORY, locations, logger);
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Create Database with Tables";
    }

}
