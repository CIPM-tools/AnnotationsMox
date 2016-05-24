package org.somox.ejbmox.pcmtx.workflow;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.mdsdprofiles.api.ProfileAPI;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcmtx.Table;
import org.somox.analyzer.simplemodelanalyzer.builder.util.DefaultResourceEnvironment;
import org.somox.ejbmox.util.EMFHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CreateTableDeploymentJob extends AbstractPCMTXJob {

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // obtain SoMoX's default resource environment
        ResourceEnvironment resourceEnvironment = DefaultResourceEnvironment.getDefaultResourceEnvironment();

        // apply Palladio.TX profile
        ProfileAPI.applyProfile(resourceEnvironment.eResource(), "PCMTransactional");

        // get (default) resource container, SoMoX creates exactly one
        List<ResourceContainer> resourceContainers = resourceEnvironment.getResourceContainer_ResourceEnvironment();
        if (resourceContainers.isEmpty()) {
            throw new IllegalStateException(
                    "Expecting at least one resource container, but found empty resource environment");
        }
        ResourceContainer resourceContainer = resourceContainers.get(0);

        // apply "DatabaseServer" stereotype to resource container
        StereotypeAPI.applyStereotype(resourceContainer, "DatabaseServer");

        // deploy tables to database server
        EList<Table> tableEList = new BasicEList<>();
        for (Table table : getPCMTXPartition().getTables()) {
            tableEList.add(table);
        }
        StereotypeAPI.setTaggedValue(resourceContainer, tableEList, "DatabaseServer", "deployedTables");

        // save modified resource environment model
        EMFHelper.save(resourceEnvironment.eResource(), logger);
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Deploy tables to default resource container";
    }

}
