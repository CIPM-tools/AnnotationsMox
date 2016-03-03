package org.somox.ejbmox.commandline;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzerConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.util.EJBmoxUtil;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class EJBmoxCommandLine {

    private static final Logger logger = Logger.getLogger(EJBmoxCommandLine.class.getSimpleName());

    private final String inputPath;
    private final String outputPath;
    private final boolean reverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour;

    public static void main(final String[] args) {
        EJBmoxUtil.initializeLogger();
        EJBmoxUtil.registerMetamodels();
        EJBmoxUtil.setupURIPathmaps();

        final EJBmoxCommandLine ejbMoxCommandLine = new EJBmoxCommandLine(args);
        ejbMoxCommandLine.runEJBmox();
    }

    public EJBmoxCommandLine(final String... args) {
        if (args.length < 2 || args.length > 4) {
            this.printUsage();
            System.exit(0);
        }
        this.inputPath = args[0];
        this.outputPath = args[1];
        this.reverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour = args.length > 2
                && args[2].equalsIgnoreCase("-rdib");
    }

    public AnalysisResult runEJBmox() {
        final long start = System.nanoTime();
        final EJBmoxAnalyzerConfiguration modelAnalyzerConfig = new EJBmoxAnalyzerConfiguration();
        final EJBmoxConfiguration configuration = new EJBmoxConfiguration();
        configuration.getFileLocations().setAnalyserInputFile(this.inputPath);
        configuration.getFileLocations().setProjectName(this.inputPath);
        configuration.getFileLocations().setOutputFolder(this.outputPath);
        configuration.setReverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour(
                this.reverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour);
        modelAnalyzerConfig.setMoxConfiguration(configuration);
        SequentialBlackboardInteractingJob<SoMoXBlackboard> ejbMoxWorkflow;
        try {
            ejbMoxWorkflow = EJBmoxUtil.createEJBmoxWorkflowJobs(modelAnalyzerConfig);
            ejbMoxWorkflow.execute(new NullProgressMonitor());
            logger.info("Finished EJBmox run in " + (System.nanoTime() - start) / 1000000000 + " seconds.");
            return ejbMoxWorkflow.getBlackboard().getAnalysisResult();
        } catch (CoreException | JobFailedException | UserCanceledException e) {
            throw new RuntimeException("Could not create and execute EJBmox workflow.", e);
        }
    }

    private void printUsage() {
        logger.info("Usage: " + EJBmoxCommandLine.class.getSimpleName() + " <inputPath> <outputPath> [<-rdib>]");
    }

}
