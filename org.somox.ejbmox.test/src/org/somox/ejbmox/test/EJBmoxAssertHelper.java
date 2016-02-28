package org.somox.ejbmox.test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.system.System;

public class EJBmoxAssertHelper {

    public static Object assertSingleEntryInCollection(final Collection<?> collection) {
        return EJBmoxAssertHelper.assertEntriesInCollection(collection, 1);
    }

    public static Object assertEntriesInCollection(final Collection<?> collection, final int entries) {
        Assert.assertEquals("There should be excactly " + entries + " element(s) in the collection " + collection,
                entries, collection.size());
        return collection.iterator().next();
    }

    public static void assertEntityName(final String expectedName, final String entityName) {
        assertEntityName(expectedName, entityName, true);
    }

    public static void assertEntityName(final String expectedName, final String entityName, final boolean exactMatch) {
        if (exactMatch) {
            Assert.assertEquals("The name of the created PCM element is wrong", expectedName, entityName);
        } else {
            Assert.assertTrue(
                    "The enitity name " + entityName + " does not containt the expected name: " + expectedName,
                    entityName.contains(expectedName));
        }

    }

    public static BasicComponent assertOneBasicComponentWithName(final Repository repository,
            final String expectedName) {
        final BasicComponent basicComponent = (BasicComponent) EJBmoxAssertHelper
                .assertSingleEntryInCollection(repository.getComponents__Repository());
        EJBmoxAssertHelper.assertEntityName(expectedName, basicComponent.getEntityName());
        return basicComponent;
    }

    public static OperationInterface assertOneInterfaceWithName(final Repository repository,
            final String expectedName) {
        final OperationInterface opInterface = (OperationInterface) EJBmoxAssertHelper
                .assertSingleEntryInCollection(repository.getInterfaces__Repository());
        EJBmoxAssertHelper.assertEntityName(expectedName, opInterface.getEntityName());
        return opInterface;
    }

    public static void assertProvidedRoleBetween(final BasicComponent bc, final OperationInterface opIf) {
        final OperationProvidedRole opr = (OperationProvidedRole) EJBmoxAssertHelper
                .assertSingleEntryInCollection(bc.getProvidedRoles_InterfaceProvidingEntity());
        Assert.assertEquals("Operation provided interface is wrong", opr.getProvidedInterface__OperationProvidedRole(),
                opIf);
    }

    public static void assertRequiredRoleBetween(final BasicComponent bc, final OperationInterface requiredInterface) {
        final OperationRequiredRole orr = (OperationRequiredRole) EJBmoxAssertHelper
                .assertSingleEntryInCollection(bc.getRequiredRoles_InterfaceRequiringEntity());
        Assert.assertEquals("Operation required interface is wrong", orr.getRequiredInterface__OperationRequiredRole(),
                requiredInterface);

    }

    public static OperationInterface claimOperationInterfaceWithName(final Repository repository, final String ifName) {
        return (OperationInterface) repository.getInterfaces__Repository().stream()
                .filter(opIf -> opIf.getEntityName().equals(ifName)).findAny().get();
    }

    public static void assertRepositoryWithOneBasicComponentAndInterface(final Repository repository) {
        final BasicComponent bc = assertOneBasicComponentWithName(repository,
                EJBmoxAbstractTest.NAME_OF_SINGLE_COMPONENT);
        final OperationInterface opIf = assertOneInterfaceWithName(repository,
                EJBmoxAbstractTest.NAME_OF_SINGLE_INTERFACE);
        assertProvidedRoleBetween(bc, opIf);
    }

    public static void assertRepositoryWithTwoComponentsAndProvidedAndRequiredInterfaces(final Repository repository) {
        assertEntriesInCollection(repository.getComponents__Repository(), 2);
        final BasicComponent bc = (BasicComponent) repository.getComponents__Repository().stream()
                .filter(comp -> comp.getEntityName().equals(EJBmoxAbstractTest.NAME_OF_SINGLE_COMPONENT)).findAny()
                .get();
        assertEntriesInCollection(repository.getInterfaces__Repository(), 2);
        final OperationInterface providedInterface = claimOperationInterfaceWithName(repository,
                EJBmoxAbstractTest.NAME_OF_PROV_INTERFACE);
        final OperationInterface requiredInterface = claimOperationInterfaceWithName(repository,
                EJBmoxAbstractTest.NAME_OF_REQ_INTERFACE);
        assertProvidedRoleBetween(bc, providedInterface);
        assertRequiredRoleBetween(bc, requiredInterface);
    }

    public static void assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(final System system) {
        assertEntriesInCollection(system.getAssemblyContexts__ComposedStructure(), 2);
        final AssemblyContext providedAseemblyContext = getAssemblyContextForComponent(system,
                EJBmoxAbstractTest.NAME_OF_REQ_COMPONENT);
        final AssemblyContext reqAseemblyContext = getAssemblyContextForComponent(system,
                EJBmoxAbstractTest.NAME_OF_SINGLE_COMPONENT);
        final List<AssemblyConnector> assemblyConnectors = system.getConnectors__ComposedStructure().stream()
                .filter(connector -> connector instanceof AssemblyConnector)
                .map(connector -> (AssemblyConnector) connector).collect(Collectors.toList());
        assertSingleEntryInCollection(assemblyConnectors);
        final AssemblyConnector assemblyConnector = assemblyConnectors.get(0);
        Assert.assertEquals(assemblyConnector.getProvidingAssemblyContext_AssemblyConnector(), providedAseemblyContext);
        Assert.assertEquals(assemblyConnector.getRequiringAssemblyContext_AssemblyConnector(), reqAseemblyContext);
    }

    public static AssemblyContext getAssemblyContextForComponent(final System system,
            final String nameOfEncapsulatedComponent) {
        return system.getAssemblyContexts__ComposedStructure().stream().filter(composedStructure -> composedStructure
                .getEncapsulatedComponent__AssemblyContext().getEntityName().equals(nameOfEncapsulatedComponent))
                .findAny().get();
    }

    public static void assertSingleAssemblyContext(final System system) {
        assertEntriesInCollection(system.getAssemblyContexts__ComposedStructure(), 1);
        final boolean exactMatch = false;
        assertEntityName(system.getAssemblyContexts__ComposedStructure().get(0).getEntityName(),
                EJBmoxAbstractTest.NAME_OF_SINGLE_COMPONENT, exactMatch);
    }

}
