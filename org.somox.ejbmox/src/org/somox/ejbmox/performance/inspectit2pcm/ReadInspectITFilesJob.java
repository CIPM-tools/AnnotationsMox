package org.somox.ejbmox.performance.inspectit2pcm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ReadInspectITFilesJob extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

    private static final Logger logger = Logger.getLogger(ReadInspectITFilesJob.class.getSimpleName());

    public static final String INSPECT_IT_FILE_PATHS = "inspectITFilePaths";

    @Override
    public void execute(final IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
        final Set<String> paths = this.getFilePaths();
    }

    private Set<String> getFilePaths() {
        final Set<String> filePaths = new HashSet<String>();
        final Object partition = this.myBlackboard.getPartition(EJBmoxConfiguration.EJBMOX_INSPECTIT_FILE_PATHS);
        if (partition instanceof Collection<?>) {
            final Collection<?> pahts = (Collection<?>) partition;
            pahts.stream().filter(obj -> obj instanceof String).map(obj -> (String) obj)
                    .forEach(filePath -> filePaths.add(filePath));
        } else {
            throw new RuntimeException("Could not find EJBinspect file it paths");
        }
        return filePaths;
    }

    @Override
    public String getName() {
        return ReadInspectITFilesJob.class.getSimpleName();
    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
    }

}
