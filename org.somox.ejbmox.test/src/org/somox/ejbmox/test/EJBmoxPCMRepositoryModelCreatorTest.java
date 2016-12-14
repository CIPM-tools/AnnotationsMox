package org.somox.ejbmox.test;

import java.util.List;

import org.palladiosimulator.pcm.repository.EventGroup;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.Repository;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.test.mock.DummyModelAnalyzer;

public class EJBmoxPCMRepositoryModelCreatorTest extends EJBmoxAbstractTest<Repository> {

    public static final int NUMBER_OF_EVENT_GROUPS= 11;
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
        EJBmoxAssertHelper.assertRepositoryWithTwoComponentsAndProvidedAndRequiredInterfaces(repository, OperationInterface.class);
    }
    
    @Override
	protected void assertTestComponentWithProvidedEventInterface(Repository repository) {
    	EJBmoxAssertHelper.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
    	List<EventGroup> eventGroups = EJBmoxAssertHelper.assertEventGroups(repository, NUMBER_OF_EVENT_GROUPS-1);
    	for (EventGroup eventGroup : eventGroups) {
			EJBmoxAssertHelper.assertSingleEntryInCollection(eventGroup.getEventTypes__EventGroup());
		}
	}
    
    @Override
	protected void assertTestTwoComponentsWithProvidedEventInterfaceAndRequiredInterface(Repository repository) {
    	EJBmoxAssertHelper.assertEntriesInCollection(repository.getComponents__Repository(), 2);
		EJBmoxAssertHelper.assertEntriesInCollection(repository.getInterfaces__Repository(), NUMBER_OF_EVENT_GROUPS);
	}

    @Override
    protected Repository executeTest(final String testMethodName) {
        EJBmoxTestUtil.executeEJBmoxPCMRepositoryModelCreator(testMethodName, this.analysisResult);

        EJBmoxTestUtil.saveReposiotryAndSourceCodeDecorator(this.analysisResult, testMethodName);

        return this.analysisResult.getInternalArchitectureModel();
    }

}
