package org.somox.ejbmox.inspectit2pcm.workflow;

import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;

public abstract class AbstractII2PCMJob extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

    public II2PCMPartition getPartition() {
        return (II2PCMPartition) getBlackboard().getPartition(II2PCMPartition.PARTITION_ID);
    }

}
