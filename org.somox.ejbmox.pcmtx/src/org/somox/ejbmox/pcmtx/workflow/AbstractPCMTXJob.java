package org.somox.ejbmox.pcmtx.workflow;

import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.inspectit2pcm.workflow.II2PCMPartition;
import org.somox.ejbmox.pcmtx.PCMTXConfiguration;

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

    public EJBmoxConfiguration getEJBMoXConfiguration() {
        return getPCMTXConfiguration().getEjbMoXConfiguration();
    }

}
