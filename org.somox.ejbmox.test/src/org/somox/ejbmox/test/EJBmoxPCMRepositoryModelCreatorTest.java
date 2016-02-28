package org.somox.ejbmox.test;

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
        EJBmoxAssertHelper.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final Repository repository) {
        EJBmoxAssertHelper.assertRepositoryWithOneBasicComponentAndInterface(repository);
    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final Repository repository) {
        EJBmoxAssertHelper.assertRepositoryWithTwoComponentsAndProvidedAndRequiredInterfaces(repository);
    }

    @Override
    protected Repository executeTest(final String testMethodName) {
        EJBmoxTestUtil.executeEJBmoxPCMRepositoryModelCreator(testMethodName, this.analysisResult);

        EJBmoxTestUtil.saveReposiotryAndSourceCodeDecorator(this.analysisResult, testMethodName);

        return this.analysisResult.getInternalArchitectureModel();
    }

}
