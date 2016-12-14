package org.somox.ejbmox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.EventGroup;
import org.palladiosimulator.pcm.repository.EventType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.SourceRole;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.commandline.EJBmoxCommandLine;
import org.somox.test.gast2seff.visitors.AssertSEFFHelper;
import org.somox.test.gast2seff.visitors.SEFFCreationHelper;

public class EJBmoxCommandLineTest extends EJBmoxAbstractTest<AnalysisResult> {

    private static final String FIND_PAST_ITEMS_IN_CATEGORY_AND_REGION = "findPastItemsInCategoryAndRegion";
    private static final String FIND_CATEGORIES_IN_REGION = "findCategoriesInRegion";
    private static final String FIND_ITEM_MAX_BID = "findItemMaxBid";
    private static final String REQUIRED_ROLE_NAME = NAME_OF_SINGLE_COMPONENT + "_requires_" + NAME_OF_REQ_INTERFACE;
	private static final String NAME_OF_REQ_EVENT = "ChangeAmountCalculatedEvent";
	private static final String EVENT_REQUIRED_ROLE_NAME = NAME_OF_SINGLE_COMPONENT + "_requires_" + NAME_OF_REQ_EVENT;

    @Override
    protected void beforeTest() {

    }

    @Override
    protected AnalysisResult executeTest(final String testMethodName) {
        final String inputPath = EJBmoxTestUtil.TEST_CODE_FOLDER_NAME + "/" + testMethodName;

        final EJBmoxCommandLine ejbCommandLine = new EJBmoxCommandLine(inputPath,
                EJBmoxTestUtil.TEST_OUTPUT_FOLDER_NAME + "/" + testMethodName);

        return ejbCommandLine.runEJBmox();
    }

    @Override
    protected void assertTestSingleComponent(final AnalysisResult analysisResult) {
        // only one component should exist
        final Repository repository = analysisResult.getInternalArchitectureModel();
        final System system = analysisResult.getSystemModel();
        EJBmoxAssertHelper.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
        EJBmoxAssertHelper.assertSingleAssemblyContext(system);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final AnalysisResult analysisResult) {
        final Repository repository = analysisResult.getInternalArchitectureModel();
        final System system = analysisResult.getSystemModel();
        EJBmoxAssertHelper.assertRepositoryWithOneBasicComponentAndInterface(repository);
        EJBmoxAssertHelper.assertSingleAssemblyContext(system);
        final List<ServiceEffectSpecification> seffs = this.findSEFFs(repository);
        final OperationInterface opInterface = (OperationInterface) repository.getInterfaces__Repository().get(0);
        Assert.assertEquals("The same number of SEFFs as the operation signatures are speficified should be created",
                opInterface.getSignatures__OperationInterface().size(), seffs.size());
        final ResourceDemandingSEFF rdSeff = this.getRDSEFFForMethod(seffs, INVESTIGATED_METHOD_NAME);

        final ResourceDemandingSEFF expectedSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStartAction());
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStopAction());
        AssertSEFFHelper.assertSeffEquals(rdSeff, expectedSEFF);
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final AnalysisResult analysisResult) {
        final Repository repository = analysisResult.getInternalArchitectureModel();
        final System system = analysisResult.getSystemModel();
        EJBmoxAssertHelper.assertRepositoryWithTwoComponentsAndProvidedAndRequiredInterfaces(repository, OperationInterface.class);
        EJBmoxAssertHelper.assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(system);

        final List<ServiceEffectSpecification> seffs = this.findSEFFs(repository);
        final ResourceDemandingSEFF rdSeff = this.getRDSEFFForMethod(seffs, INVESTIGATED_METHOD_NAME);

        // find necessary infos to create expected SEFF
        final BasicComponent basicComponent = (BasicComponent) SEFFCreationHelper
                .findComponentInPCMRepository(NAME_OF_SINGLE_COMPONENT, repository);
        final OperationRequiredRole operationRequiredRole = SEFFCreationHelper
                .findOperaitonRequiredRoleInBasicComponent(basicComponent, REQUIRED_ROLE_NAME);
        final OperationInterface requiredOperationInterface = operationRequiredRole
                .getRequiredInterface__OperationRequiredRole();
        final OperationSignature findItemMaxBidSig = SEFFCreationHelper
                .findOperationSignatureInInterface(FIND_ITEM_MAX_BID, requiredOperationInterface);
        final OperationSignature findCategoriesInRegionSig = SEFFCreationHelper
                .findOperationSignatureInInterface(FIND_CATEGORIES_IN_REGION, requiredOperationInterface);
        final OperationSignature findPastItemsInCategoryAndRegion = SEFFCreationHelper
                .findOperationSignatureInInterface(FIND_PAST_ITEMS_IN_CATEGORY_AND_REGION, requiredOperationInterface);

        // create expected SEFF
        final ResourceDemandingSEFF expectedSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStartAction());
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour()
                .add(SEFFCreationHelper.createExternalCallAction(operationRequiredRole, findItemMaxBidSig));
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour().add(
                SEFFCreationHelper.createExternalCallAction(operationRequiredRole, findPastItemsInCategoryAndRegion));
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour()
                .add(SEFFCreationHelper.createExternalCallAction(operationRequiredRole, findCategoriesInRegionSig));
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStopAction());

        AssertSEFFHelper.assertSeffEquals(rdSeff, expectedSEFF);
    }
    
    @Override
	protected void assertTestComponentWithProvidedEventInterface(AnalysisResult analysisResult) {
    	final List<ServiceEffectSpecification> seffs = this.findSEFFs(analysisResult.getInternalArchitectureModel());
    	final ResourceDemandingSEFF rdSeff = this.getRDSEFFForMethod(seffs, "SaleStartedEvent");
    	
    	// create expected SEFF
    	final ResourceDemandingSEFF expectedSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStartAction());
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStopAction());

        AssertSEFFHelper.assertSeffEquals(rdSeff, expectedSEFF);
	}
    
    @Override
	protected void assertTestTwoComponentsWithProvidedEventInterfaceAndRequiredInterface(AnalysisResult analysisResult) {
    	Repository repository = analysisResult.getInternalArchitectureModel();
    	EJBmoxAssertHelper.assertEntriesInCollection(repository.getComponents__Repository(), 2);
    	EJBmoxAssertHelper.assertEventGroups(repository,EJBmoxPCMRepositoryModelCreatorTest.NUMBER_OF_EVENT_GROUPS);
    	
    	final List<ServiceEffectSpecification> seffs = this.findSEFFs(repository);
        final ResourceDemandingSEFF rdSeff = this.getRDSEFFForMethod(seffs, "SaleStartedEvent");
    	
        // find necessary infos to create expected SEFF
        final BasicComponent basicComponent = (BasicComponent) SEFFCreationHelper
                .findComponentInPCMRepository(NAME_OF_SINGLE_COMPONENT, repository);
        final SourceRole sourceRole = SEFFCreationHelper
                .findSourceRoleInBasicComponent(basicComponent, EVENT_REQUIRED_ROLE_NAME);
        final EventGroup requiredEventGroup = sourceRole.getEventGroup__SourceRole();
        final EventType onEventType = SEFFCreationHelper
                .findEventTypeInEventGroup(NAME_OF_REQ_EVENT, requiredEventGroup);
    	
    	// create expected SEFF
        final ResourceDemandingSEFF expectedSEFF = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStartAction());
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour()
                .add(SEFFCreationHelper.createEmitEventCallAction(sourceRole, onEventType));
        SEFFCreationHelper.createAndAddInternalActionToSeff(expectedSEFF);
        expectedSEFF.getSteps_Behaviour().add(
                SEFFCreationHelper.createEmitEventCallAction(sourceRole, onEventType));
        expectedSEFF.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStopAction());
        
        AssertSEFFHelper.assertSeffEquals(rdSeff, expectedSEFF);
	}

    private ResourceDemandingSEFF getRDSEFFForMethod(final List<ServiceEffectSpecification> seffs,
            final String investigatedMethodName) {
        final ResourceDemandingSEFF rdseff = (ResourceDemandingSEFF) seffs.stream()
                .filter(seff -> seff.getDescribedService__SEFF().getEntityName().equals(investigatedMethodName))
                .findAny().get();
        return rdseff;
    }

    private List<ServiceEffectSpecification> findSEFFs(final Repository repository) {
        return this.findComponentsWithSEFFs(repository)
                .map(basicComp -> basicComp.getServiceEffectSpecifications__BasicComponent())
                .collect(Collectors.toList()).stream().flatMap(seffList -> seffList.stream())
                .collect(Collectors.toList());
    }

    private Stream<BasicComponent> findComponentsWithSEFFs(final Repository repository) {
        return repository.getComponents__Repository().stream().map(component -> (BasicComponent) component)
                .filter(basicComp -> !basicComp.getServiceEffectSpecifications__BasicComponent().isEmpty());
    }


}
