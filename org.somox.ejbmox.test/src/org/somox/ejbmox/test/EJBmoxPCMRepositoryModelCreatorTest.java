package org.somox.ejbmox.test;

import org.junit.Assert;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.test.mock.DummyModelAnalyzer;

public class EJBmoxPCMRepositoryModelCreatorTest extends EJBmoxAbstractTest<Repository> {

    private AnalysisResult analysisResult;

    @Override
    protected void beforeTest() {
        final DummyModelAnalyzer dummyModelAnalyzer = new DummyModelAnalyzer();
        this.analysisResult = dummyModelAnalyzer.initializeAnalysisResult();
    }

    @Override
    protected void assertTestSingleComponent(final Repository repository) {
        this.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final Repository repository) {
        final BasicComponent bc = this.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
        final OperationInterface opIf = this.assertOneInterfaceWithName(repository, NAME_OF_SINGLE_INTERFACE);
        this.assertProvidedRoleBetween(bc, opIf);
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final Repository repository) {
        EJBmoxAssertHelper.assertEntriesInCollection(repository.getComponents__Repository(), 2);
        final BasicComponent bc = (BasicComponent) repository.getComponents__Repository().stream()
                .filter(comp -> comp.getEntityName().equals(NAME_OF_SINGLE_COMPONENT)).findAny().get();
        EJBmoxAssertHelper.assertEntriesInCollection(repository.getInterfaces__Repository(), 2);
        final OperationInterface providedInterface = this.claimOperationInterfaceWithName(repository,
                NAME_OF_PROV_INTERFACE);
        final OperationInterface requiredInterface = this.claimOperationInterfaceWithName(repository,
                NAME_OF_REQ_INTERFACE);
        this.assertProvidedRoleBetween(bc, providedInterface);
        this.assertRequiredRoleBetween(bc, requiredInterface);
    }

    @Override
    protected Repository executeTest(final String testMethodName) {
        EJBmoxTestUtil.executeEJBmoxPCMRepositoryModelCreator(testMethodName, this.analysisResult);

        EJBmoxTestUtil.saveReposiotryAndSourceCodeDecorator(this.analysisResult, testMethodName);

        return this.analysisResult.getInternalArchitectureModel();
    }

    private BasicComponent assertOneBasicComponentWithName(final Repository repository, final String expectedName) {
        final BasicComponent basicComponent = (BasicComponent) EJBmoxAssertHelper
                .assertSingleEntryInCollection(repository.getComponents__Repository());
        EJBmoxAssertHelper.assertEntityName(expectedName, basicComponent.getEntityName());
        return basicComponent;
    }

    private OperationInterface assertOneInterfaceWithName(final Repository repository, final String expectedName) {
        final OperationInterface opInterface = (OperationInterface) EJBmoxAssertHelper
                .assertSingleEntryInCollection(repository.getInterfaces__Repository());
        EJBmoxAssertHelper.assertEntityName(expectedName, opInterface.getEntityName());
        return opInterface;
    }

    private void assertProvidedRoleBetween(final BasicComponent bc, final OperationInterface opIf) {
        final OperationProvidedRole opr = (OperationProvidedRole) EJBmoxAssertHelper
                .assertSingleEntryInCollection(bc.getProvidedRoles_InterfaceProvidingEntity());
        Assert.assertEquals("Operation provided interface is wrong", opr.getProvidedInterface__OperationProvidedRole(),
                opIf);
    }

    private void assertRequiredRoleBetween(final BasicComponent bc, final OperationInterface requiredInterface) {
        final OperationRequiredRole orr = (OperationRequiredRole) EJBmoxAssertHelper
                .assertSingleEntryInCollection(bc.getRequiredRoles_InterfaceRequiringEntity());
        Assert.assertEquals("Operation required interface is wrong", orr.getRequiredInterface__OperationRequiredRole(),
                requiredInterface);

    }

    private OperationInterface claimOperationInterfaceWithName(final Repository repository, final String ifName) {
        return (OperationInterface) repository.getInterfaces__Repository().stream()
                .filter(opIf -> opIf.getEntityName().equals(ifName)).findAny().get();
    }

}
