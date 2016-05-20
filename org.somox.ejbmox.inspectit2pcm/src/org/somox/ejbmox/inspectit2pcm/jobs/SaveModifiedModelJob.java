package org.somox.ejbmox.inspectit2pcm.jobs;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class SaveModifiedModelJob extends AbstractII2PCMJob {

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		Resource repositoryResource = getBlackboard().getAnalysisResult().getInternalArchitectureModel().eResource();
		try {
			final HashMap<Object, Object> saveOptions = new HashMap<>();
			saveOptions.put(XMIResource.OPTION_URI_HANDLER, new URIHandlerImpl.PlatformSchemeAware());
			repositoryResource.save(saveOptions);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// nothing to do
	}

	@Override
	public String getName() {
		return "Save parametrized PCM model";
	}

}
