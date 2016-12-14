package org.somox.ejbmox.test;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.palladiosimulator.pcm.system.System;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzer;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;

public class EJBmoxAnalyzerTest extends EJBmoxAbstractTest<System> {

    private EJBmoxAnalyzer ejbMoxAnalyzer;

    @Override
    protected void beforeTest() {
        this.ejbMoxAnalyzer = new EJBmoxAnalyzer();

    }

    @Override
    protected System executeTest(final String testMethodName) {
        final EJBmoxConfiguration ejbmoxConfiguration = new EJBmoxConfiguration();
        ejbmoxConfiguration.getFileLocations().setProjectNames(
                new HashSet<String>(Arrays.asList(EJBmoxTestUtil.TEST_CODE_FOLDER_NAME + "/" + testMethodName)));
        try {
            final AnalysisResult analysisResult = this.ejbMoxAnalyzer.analyze(ejbmoxConfiguration, null,
                    new NullProgressMonitor());
            EJBmoxTestUtil.saveRepoSourceCodeDecoratorAndSystem(analysisResult, testMethodName);
            return analysisResult.getSystemModel();

        } catch (final ModelAnalyzerException e) {
            throw new RuntimeException("ModelAnalyzer failed.", e);
        }
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final System system) {
        EJBmoxAssertHelper.assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(system);
    }

    @Override
    protected void assertTestSingleComponent(final System system) {
        EJBmoxAssertHelper.assertSingleAssemblyContext(system);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final System system) {
        EJBmoxAssertHelper.assertSingleAssemblyContext(system);
    }
    
    @Override
	protected void assertTestComponentWithProvidedEventInterface(final System system) {
    	EJBmoxAssertHelper.assertSingleAssemblyContext(system);
	}

	@Override
	protected void assertTestTwoComponentsWithProvidedEventInterfaceAndRequiredInterface(System system) {
		fail("Not implemented yet");
		//EJBmoxAssertHelper.assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(system);
	}
    
}
