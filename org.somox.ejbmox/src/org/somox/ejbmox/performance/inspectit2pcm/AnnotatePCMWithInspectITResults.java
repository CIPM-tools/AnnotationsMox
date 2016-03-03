package org.somox.ejbmox.performance.inspectit2pcm;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class AnnotatePCMWithInspectITResults extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

    @Override
    public void execute(final IProgressMonitor arg0) throws JobFailedException, UserCanceledException {

    }

    @Override
    public String getName() {
        return AnnotatePCMWithInspectITResults.class.getSimpleName();
    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
    }

}
