package org.somox.ejbmox.pcmtx;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.pcmtx.workflow.CreateEntityTypesJob;
import org.somox.ejbmox.pcmtx.workflow.PCMTXPartition;
import org.somox.ejbmox.pcmtx.workflow.ParseSQLJob;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PCMTXJob extends AbstractWorkflowExtensionJob<SoMoXBlackboard> {

    public PCMTXJob() {
        // 1. Parse SQL statements
        addJob(new ParseSQLJob());

        // 2. ...
        addJob(new CreateEntityTypesJob());

        // x. Save
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
