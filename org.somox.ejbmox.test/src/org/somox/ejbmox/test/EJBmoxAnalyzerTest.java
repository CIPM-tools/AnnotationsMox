package org.somox.ejbmox.test;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.system.System;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.configuration.SoMoXConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzer;

public class EJBmoxAnalyzerTest extends EJBmoxAbstractTest<System> {

    private EJBmoxAnalyzer ejbMoxAnalyzer;

    @Override
    protected void beforeTest() {
        this.ejbMoxAnalyzer = new EJBmoxAnalyzer();

    }

    @Override
    protected System executeTest(final String testMethodName) {
        final SoMoXConfiguration somoxConfiguration = new SoMoXConfiguration();
        somoxConfiguration.getFileLocations().setProjectName(TEST_CODE_FOLDER_NAME + "/" + testMethodName);
        try {
            final AnalysisResult analysisResult = this.ejbMoxAnalyzer.analyze(somoxConfiguration, null,
                    new NullProgressMonitor());
            EJBmoxTestUtil.saveRepoSourceCodeDecoratorAndSystem(analysisResult, testMethodName);
            return analysisResult.getSystemModel();

        } catch (final ModelAnalyzerException e) {
            throw new RuntimeException("ModelAnalyzer failed.", e);
        }
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final System system) {
        EJBmoxAssertHelper.assertEntriesInCollection(system.getAssemblyContexts__ComposedStructure(), 2);
        final AssemblyContext providedAseemblyContext = this.getAssemblyContextForComponent(system,
                NAME_OF_REQ_COMPONENT);
        final AssemblyContext reqAseemblyContext = this.getAssemblyContextForComponent(system,
                NAME_OF_SINGLE_COMPONENT);
        final List<AssemblyConnector> assemblyConnectors = system.getConnectors__ComposedStructure().stream()
                .filter(connector -> connector instanceof AssemblyConnector)
                .map(connector -> (AssemblyConnector) connector).collect(Collectors.toList());
        EJBmoxAssertHelper.assertSingleEntryInCollection(assemblyConnectors);
        final AssemblyConnector assemblyConnector = assemblyConnectors.get(0);
        Assert.assertEquals(assemblyConnector.getProvidingAssemblyContext_AssemblyConnector(), providedAseemblyContext);
        Assert.assertEquals(assemblyConnector.getRequiringAssemblyContext_AssemblyConnector(), reqAseemblyContext);
    }

    private AssemblyContext getAssemblyContextForComponent(final System system,
            final String nameOfEncapsulatedComponent) {
        return system.getAssemblyContexts__ComposedStructure().stream().filter(composedStructure -> composedStructure
                .getEncapsulatedComponent__AssemblyContext().getEntityName().equals(nameOfEncapsulatedComponent))
                .findAny().get();
    }

    @Override
    protected void assertTestSingleComponent(final System system) {
        this.assertSingleAssemblyContext(system);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final System system) {
        this.assertSingleAssemblyContext(system);
    }

    private void assertSingleAssemblyContext(final System system) {
        EJBmoxAssertHelper.assertEntriesInCollection(system.getAssemblyContexts__ComposedStructure(), 1);
        final boolean exactMatch = false;
        EJBmoxAssertHelper.assertEntityName(system.getAssemblyContexts__ComposedStructure().get(0).getEntityName(),
                NAME_OF_SINGLE_COMPONENT, exactMatch);
    }

}
