package org.somox.ejbmox.pcmtx.workflow;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.core.entity.EntityFactory;
import org.palladiosimulator.pcm.core.entity.ResourceRequiredRole;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.resourcetype.ResourceInterface;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.seff_performance.ResourceCall;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;
import org.palladiosimulator.pcmtx.EntityType;
import org.palladiosimulator.pcmtx.api.EntityAccessType;
import org.palladiosimulator.pcmtx.api.EntityTypesAPI;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;
import org.somox.ejbmox.pcmtx.model.ParsedSQLStatement;
import org.somox.ejbmox.pcmtx.model.StatementType;
import org.somox.ejbmox.util.EMFHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CreateEntityAccessesJob extends AbstractPCMTXJob {

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        Map<InternalAction, SQLStatement> internalAction2Stmt = getII2PCMPartition().getTrace()
                .getInternalActionToStatementLinks();
        for (Entry<InternalAction, SQLStatement> e : internalAction2Stmt.entrySet()) {
            InternalAction action = e.getKey();
            SQLStatement stmt = e.getValue();
            ParsedSQLStatement parsedStmt = getPCMTXPartition().getParsedStatementsMap().get(stmt);

            for (String accessedTableName : parsedStmt.getTableNames()) {
                EntityType entityType = getPCMTXPartition().getTableNameToEntityTypeMap().get(accessedTableName);

                // add suitable resource required role to enclosing component, if not created before
                BasicComponent component = PCMHelper.findSeffForInternalAction(action)
                        .getBasicComponent_ServiceEffectSpecification();
                ResourceInterface requiredInterface = EntityTypesAPI.getProvidedInterface(entityType);
                ResourceRequiredRole requiredRole = ensureComponentRequiresInterface(component, requiredInterface);

                // add resource call + entity access annotation to internal action
                ResourceCall resourceCall = SeffPerformanceFactory.eINSTANCE.createResourceCall();
                resourceCall.setNumberOfCalls__ResourceCall(PCMHelper.createPCMRandomVariable(1)); // TODO
                resourceCall.setEntityName("Access entity type " + entityType.getEntityName());
                resourceCall.setResourceRequiredRole__ResourceCall(requiredRole);
                EntityAccessType accessType = statementTypeToAccessType(parsedStmt.getType(), logger);
                resourceCall.setSignature__ResourceCall(
                        EntityTypesAPI.findSignatureByAccessType(requiredInterface, accessType));
                action.getResourceCall__Action().add(resourceCall);

                // apply "EntityAccess" stereotype to resource call
                StereotypeAPI.applyStereotype(resourceCall, "EntityAccess");
            }
        }

        // save modified repository
        Resource repositoryResource = getBlackboard().getAnalysisResult().getInternalArchitectureModel().eResource();
        EMFHelper.save(repositoryResource, logger);
    }

    private static EntityAccessType statementTypeToAccessType(StatementType stmtType, Logger logger) {
        switch (stmtType) {
        case SELECT:
            return EntityAccessType.READ;
        case INSERT:
            return EntityAccessType.INSERT;
        case UPDATE:
            return EntityAccessType.UPDATE;
        case OTHER:
            // fall through
        default:
            logger.error("Unsupported Statement type: " + stmtType);
            return EntityAccessType.READ; // fallback solution
        }
    }

    private static ResourceRequiredRole ensureComponentRequiresInterface(BasicComponent component,
            ResourceInterface requiredInterface) {
        ResourceRequiredRole requiredRole = null;
        for (ResourceRequiredRole role : component.getResourceRequiredRoles__ResourceInterfaceRequiringEntity()) {
            if (requiredInterface.equals(role.getRequiredResourceInterface__ResourceRequiredRole())) {
                requiredRole = role;
                break;
            }
        }
        if (requiredRole == null) {
            requiredRole = EntityFactory.eINSTANCE.createResourceRequiredRole();
            requiredRole.setEntityName("Require resource interface " + requiredInterface.getEntityName());
            requiredRole.setRequiredResourceInterface__ResourceRequiredRole(requiredInterface);
            requiredRole.setResourceInterfaceRequiringEntity__ResourceRequiredRole(component);
        }
        return requiredRole;
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Create entity access stereotypes";
    }

}
