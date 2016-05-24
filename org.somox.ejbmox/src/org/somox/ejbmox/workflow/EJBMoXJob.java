package org.somox.ejbmox.workflow;

import java.util.Map;

import org.somox.analyzer.simplemodelanalyzer.jobs.SaveSoMoXModelsJob;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzerConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.ejb.functionclassification.EJBmoxFunctionClassificationStrategyFactory;
import org.somox.gast2seff.jobs.GAST2SEFFJob;

import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public class EJBMoXJob extends AbstractExtendableJob<SoMoXBlackboard> {

	private EJBmoxAnalyzerConfiguration modelAnalyzerConfig; 
	
	public EJBMoXJob(final EJBmoxAnalyzerConfiguration modelAnalyzerConfig) {
		this.modelAnalyzerConfig = modelAnalyzerConfig;
        final SoMoXBlackboard soMoXBlackboard = new SoMoXBlackboard();
        setBlackboard(soMoXBlackboard);
        final EJBmoxConfiguration ejbmoxConfiguration = modelAnalyzerConfig.getMoxConfiguration();
        soMoXBlackboard.addPartition(EJBmoxConfiguration.EJBMOX_INSPECTIT_FILE_PATHS,
                ejbmoxConfiguration.getInspectITFilePaths());

        add(new EJBmoxAnalzerJob(modelAnalyzerConfig));

        final boolean reverseEngineerResourceDemandingInternalBehaviour = ejbmoxConfiguration
                .isReverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour();
        add(new GAST2SEFFJob(reverseEngineerResourceDemandingInternalBehaviour,
                new EJBmoxFunctionClassificationStrategyFactory()));

        if (null != ejbmoxConfiguration.getInspectITFilePaths()
                && 0 < ejbmoxConfiguration.getInspectITFilePaths().size()) {
        	// TODO
        }
        
        handleJobExtensions(EJBMoXWorkflowHooks.PRE_SAVE_MODELS, new Configuration());

        add(new SaveSoMoXModelsJob(modelAnalyzerConfig.getMoxConfiguration()));
        
        handleJobExtensions(EJBMoXWorkflowHooks.POST_SAVE_MODELS, new Configuration());
	}
	
	private class Configuration implements ExtendableJobConfiguration {

		@Override
		public Map<String, Object> getAttributes() {
			return modelAnalyzerConfig.getMoxConfiguration().getAttributes();
		}
		
	}
	
}
