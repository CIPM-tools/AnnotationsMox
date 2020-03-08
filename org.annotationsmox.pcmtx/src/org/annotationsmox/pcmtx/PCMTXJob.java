package org.annotationsmox.pcmtx;

import org.annotationsmox.pcmtx.workflow.CreateDatabaseWithTablesJob;
import org.annotationsmox.pcmtx.workflow.CreateEntityAccessesJob;
import org.annotationsmox.pcmtx.workflow.CreateEntityTypesJob;
import org.annotationsmox.pcmtx.workflow.CreateTableDeploymentJob;
import org.annotationsmox.pcmtx.workflow.PCMTXPartition;
import org.annotationsmox.pcmtx.workflow.ParseSQLJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PCMTXJob extends AbstractWorkflowExtensionJob<SoMoXBlackboard> {

    public PCMTXJob() {
        // 1. Parse SQL statements
        addJob(new ParseSQLJob());

        // 2. Create resourcetype model with entity types
        addJob(new CreateEntityTypesJob());

        // 3. Create pcmtx model describing the database schema
        addJob(new CreateDatabaseWithTablesJob());

        // 4. Annotate internal actions with entity accesses
        addJob(new CreateEntityAccessesJob());

        // 5. Deploy tables to (default) resource container acting as database server
        addJob(new CreateTableDeploymentJob());
    }

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        PCMTXConfiguration config = (PCMTXConfiguration) getJobConfiguration();

        // create new blackboard partition based on current blackboard contents
        PCMTXPartition partition = new PCMTXPartition(config);
        getBlackboard().addPartition(PCMTXPartition.PARTITION_ID, partition);

        // execute child jobs
        super.execute(monitor);
    }

    @Override
    public String getName() {
        return PCMTXJob.class.getSimpleName();
    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
        // nothing to do
    }

}
