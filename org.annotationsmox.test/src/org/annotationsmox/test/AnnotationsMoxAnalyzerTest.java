package org.annotationsmox.test;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.annotationsmox.analyzer.AnnotationsMoxAnalyzer;
import org.annotationsmox.analyzer.AnnotationsMoxConfiguration;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.palladiosimulator.pcm.system.System;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzerException;

public class AnnotationsMoxAnalyzerTest extends AnnotationsMoxAbstractTest<System> {

    private AnnotationsMoxAnalyzer ejbMoxAnalyzer;

    @Override
    protected void beforeTest() {
        this.ejbMoxAnalyzer = new AnnotationsMoxAnalyzer();

    }

    @Override
    protected System executeTest(final String testMethodName) {
        final AnnotationsMoxConfiguration ejbmoxConfiguration = new AnnotationsMoxConfiguration();
        ejbmoxConfiguration.getFileLocations().setProjectNames(
                new HashSet<String>(Arrays.asList(AnnotationsMoxTestUtil.TEST_CODE_FOLDER_NAME + "/" + testMethodName)));
        try {
            final AnalysisResult analysisResult = this.ejbMoxAnalyzer.analyze(ejbmoxConfiguration, null,
                    new NullProgressMonitor());
            AnnotationsMoxTestUtil.saveRepoSourceCodeDecoratorAndSystem(analysisResult, testMethodName);
            return analysisResult.getSystemModel();

        } catch (final ModelAnalyzerException e) {
            throw new RuntimeException("ModelAnalyzer failed.", e);
        }
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final System system) {
        AnnotationsMoxAssertHelper.assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(system);
    }

    @Override
    protected void assertTestSingleComponent(final System system) {
        AnnotationsMoxAssertHelper.assertSingleAssemblyContext(system);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final System system) {
        AnnotationsMoxAssertHelper.assertSingleAssemblyContext(system);
    }
    
    @Override
	protected void assertTestComponentWithProvidedEventInterface(final System system) {
    	AnnotationsMoxAssertHelper.assertSingleAssemblyContext(system);
	}

	@Override
	protected void assertTestTwoComponentsWithProvidedEventInterfaceAndRequiredInterface(System system) {
		fail("Not implemented yet");
		//EJBmoxAssertHelper.assertSystemWithTwoComponentsWithProvidedAndRequiredInterface(system);
	}
    
}
