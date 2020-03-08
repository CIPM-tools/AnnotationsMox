package org.annotationsmox.pcmtx.workflow;

import org.annotationsmox.analyzer.AnnotationsMoxConfiguration;
import org.annotationsmox.inspectit2pcm.workflow.II2PCMPartition;
import org.annotationsmox.pcmtx.PCMTXConfiguration;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;

public abstract class AbstractPCMTXJob extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

    public PCMTXPartition getPCMTXPartition() {
        return (PCMTXPartition) getBlackboard().getPartition(PCMTXPartition.PARTITION_ID);
    }

    public II2PCMPartition getII2PCMPartition() {
        return (II2PCMPartition) getBlackboard().getPartition(II2PCMPartition.PARTITION_ID);
    }

    public PCMTXConfiguration getPCMTXConfiguration() {
        return getPCMTXPartition().getConfiguration();
    }

    public AnnotationsMoxConfiguration getEJBMoXConfiguration() {
        return getPCMTXConfiguration().getEjbMoXConfiguration();
    }

}
