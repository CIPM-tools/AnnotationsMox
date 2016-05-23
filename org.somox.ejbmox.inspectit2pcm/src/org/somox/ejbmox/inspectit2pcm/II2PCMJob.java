package org.somox.ejbmox.inspectit2pcm;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.inspectit2pcm.workflow.II2PCMPartition;
import org.somox.ejbmox.inspectit2pcm.workflow.ParametrizationFromMonitoringResultsJob;
import org.somox.ejbmox.inspectit2pcm.workflow.ParametrizeModelJob;
import org.somox.ejbmox.inspectit2pcm.workflow.SaveModifiedModelJob;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

/**
 * The main job, which hooks into EJBMoX's workflow.
 * 
 * @author Philipp Merkle
 *
 */
public class II2PCMJob extends AbstractWorkflowExtensionJob<SoMoXBlackboard> {

	public II2PCMJob() {
		// 1. Obtain PCM parametrization from InspectIT monitoring results 
		addJob(new ParametrizationFromMonitoringResultsJob());
		
		// 2. Parametrize PCM model according to parametrization
		addJob(new ParametrizeModelJob());
		
		// 3. Save modified model
		addJob(new SaveModifiedModelJob());
	}

	@Override
	public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		II2PCMConfiguration config = (II2PCMConfiguration) getJobConfiguration();

		// create new blackboard partition based on current blackboard contents
		SoMoXBlackboard blackboard = getBlackboard();
		II2PCMPartition partition = II2PCMPartition.createFrom(blackboard, config);
		getBlackboard().addPartition(II2PCMPartition.PARTITION_ID, partition);

		// execute child jobs
		super.execute(monitor);
	}

	@Override
	public String getName() {
		return II2PCMJob.class.getSimpleName();
	}

	@Override
	public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
		// nothing to do
	}

}
