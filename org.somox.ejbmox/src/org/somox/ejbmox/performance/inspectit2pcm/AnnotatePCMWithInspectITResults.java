package org.somox.ejbmox.performance.inspectit2pcm;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.emftext.language.java.members.ClassMethod;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.inspectit2pcm.InspectIT2PCM;
import org.somox.ejbmox.inspectit2pcm.InspectIT2PCMConfiguration;
import org.somox.sourcecodedecorator.SEFF2MethodMapping;
import org.somox.sourcecodedecorator.SourceCodeDecoratorRepository;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class AnnotatePCMWithInspectITResults extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

	private static final Logger logger = Logger.getLogger(AnnotatePCMWithInspectITResults.class);

	@Override
	public void execute(final IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
		SourceCodeDecoratorRepository sourceDecorator = getBlackboard().getAnalysisResult()
				.getSourceCodeDecoratorRepository();

		// build list of external services, together with their fully qualified
		// name (FQN)
		Map<ResourceDemandingSEFF, String> seffToFQNMap = new HashMap<>();
		for (SEFF2MethodMapping m : sourceDecorator.getSeff2MethodMappings()) {
			String fqn = fullyQualifiedName((ClassMethod) m.getStatementListContainer());
			seffToFQNMap.put((ResourceDemandingSEFF) m.getSeff(), fqn);
			logger.debug("Adding SEFF with FQN " + fqn);
		}

		// parametrize SEFFs
		InspectIT2PCMConfiguration config = new InspectIT2PCMConfiguration();
		InspectIT2PCM iit2pcm = new InspectIT2PCM(config);
		iit2pcm.parametrizeSEFFs(seffToFQNMap);
	}

	private static String fullyQualifiedName(ClassMethod method) {
		return method.getContainingCompilationUnit().getContainedClass().getQualifiedName() + "." + method.getName();
	}

	@Override
	public String getName() {
		return AnnotatePCMWithInspectITResults.class.getSimpleName();
	}

	@Override
	public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
	}

}
