package org.somox.ejbmox.inspectit2pcm;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.RESTClient;

/**
 * 
 * @author Philipp Merkle
 *
 */
public class TestExtractor {

    private static Set<String> servicesFQN = new HashSet<>();

    private static Set<String> interfacesFQN = new HashSet<>();

    static {
        servicesFQN.add("de.hpi.sam.rubis.bidandbuy.impl.BuyNowServiceBean.buyItemNow");
        servicesFQN.add("de.hpi.sam.rubis.authservice.impl.AuthenticationServiceBean.authenticate");
        servicesFQN.add("de.hpi.sam.rubis.queryservice.impl.QueryServiceBean.findItemBidHistory");
        servicesFQN.add("de.hpi.sam.rubis.queryservice.impl.QueryServiceBean.retrieveAvailabilityOfItem");
        servicesFQN.add("de.hpi.sam.rubis.queryservice.impl.BasicQueryServiceBean.findItemById");
        servicesFQN.add("de.hpi.sam.rubis.queryservice.impl.BasicQueryServiceBean.findUserByNickname");
        servicesFQN.add("de.hpi.sam.rubis.inventorymgmt.impl.InventoryServiceBean.checkAvailabilityOfItem");
        servicesFQN.add("de.hpi.sam.rubis.inventorymgmt.impl.InventoryServiceBean.reserveItem");
        servicesFQN.add("de.hpi.sam.rubis.persistenceservice.impl.BusinessObjectsPersistenceServiceBean.persistBuyNow");
        servicesFQN.add(
                "de.hpi.sam.rubis.persistenceservice.impl.BusinessObjectsPersistenceServiceBean.reduceInventoryItem");

        interfacesFQN.add("de.hpi.sam.rubis.authservice.AuthenticationService");
        interfacesFQN.add("de.hpi.sam.rubis.bidandbuy.BidService");
        interfacesFQN.add("de.hpi.sam.rubis.bidandbuy.BuyNowService");
        interfacesFQN.add("de.hpi.sam.rubis.inventorymgmt.InventoryService");
        interfacesFQN.add("de.hpi.sam.rubis.itemmgmt.BrowseCategoriesService");
        interfacesFQN.add("de.hpi.sam.rubis.itemmgmt.ItemRegistrationService");
        interfacesFQN.add("de.hpi.sam.rubis.persistenceservice.BusinessObjectsPersistenceService");
        interfacesFQN.add("de.hpi.sam.rubis.queryservice.BasicQueryService");
        interfacesFQN.add("de.hpi.sam.rubis.queryservice.QueryService");
        interfacesFQN.add("de.hpi.sam.rubis.reputationservice.ReputationService");
        // TODO 3 missing (from de.hpi.sam.rubis.reputationservice)

    }

    public static void main(String[] args) throws IOException {
        // log4j basic setup
        BasicConfigurator.configure();

        RESTClient client = new RESTClient("http://localhost:8182/rest/");
        IdentsServiceClient identService = new IdentsServiceClient(client);
        InvocationsServiceClient invocationsService = new InvocationsServiceClient(client);

        InvocationTreeScanner scanner = new InvocationTreeScanner(new ConsoleOutputTraversalListener(), servicesFQN,
                interfacesFQN, identService, invocationsService);

        List<Long> invocationIds = invocationsService.getInvocationSequencesId();
        for (long invocationId : invocationIds) {
            InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
            scanner.scanInvocationTree(invocation);
        }
    }

    public static class ConsoleOutputTraversalListener implements ScanningProgressListener {

        @Override
        public void systemCallBegin(MethodIdent calledService, double time) {
            System.out.println("BEGIN SYSTEM CALL " + calledService.toFQN() + " @ " + time);
        }

        @Override
        public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
            System.out.println(callingService.toFQN() + "--END EXTERNAL CALL " + calledService.toFQN() + " @ " + time);
        }

        @Override
        public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
            System.out
                    .println(callingService.toFQN() + "--BEGIN EXTERNAL CALL " + calledService.toFQN() + " @ " + time);
        }

        @Override
        public void internalActionBegin(MethodIdent callingService, double time) {
            System.out.println(callingService.toFQN() + "--BEGIN INTERNAL ACTION " + " @ " + time);
        }

        @Override
        public void internalActionEnd(MethodIdent callingService, double time) {
            System.out.println(callingService.toFQN() + "--END INTERNAL ACTION " + " @ " + time);
        }

        @Override
        public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
            System.out.println(callingService.toFQN() + "--SQL STATEMENT " + statement);
        }

        @Override
        public void systemCallEnd(MethodIdent calledService, double time) {
            System.out.println("END SYSTEM CALL " + calledService.toFQN() + " @ " + time);
            System.out.println("----------------");
        }

    }

}
