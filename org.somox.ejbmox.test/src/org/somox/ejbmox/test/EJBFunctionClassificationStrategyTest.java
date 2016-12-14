package org.somox.ejbmox.test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.statements.Statement;
import org.junit.Assert;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.ejb.functionclassification.EJBFunctionClassificationStrategy;
import org.somox.ejbmox.test.mock.DummyModelAnalyzer;
import org.somox.gast2seff.visitors.FunctionCallClassificationVisitor;
import org.somox.gast2seff.visitors.FunctionCallClassificationVisitor.FunctionCallType;
import org.somox.gast2seff.visitors.MethodCallFinder;
import org.somox.sourcecodedecorator.SEFF2MethodMapping;

public class EJBFunctionClassificationStrategyTest extends EJBmoxAbstractTest<List<BitSet>> {

    @Override
    protected void beforeTest() {
    }

    @Override
    protected List<BitSet> executeTest(final String testMethodName) {
        final AnalysisResult analysisResult = new DummyModelAnalyzer().initializeAnalysisResult();
        EJBmoxTestUtil.executeEJBmoxPCMRepositoryModelCreator(testMethodName, analysisResult);

        // classify calls for reserveItem of InventoryServiceBean
        final SEFF2MethodMapping reserveItemMethodMapping = analysisResult.getSourceCodeDecoratorRepository()
                .getSeff2MethodMappings().stream()
                .filter(seff2MethodMapping -> seff2MethodMapping.getStatementListContainer() instanceof Method
                        && ((Method) seff2MethodMapping.getStatementListContainer()).getName()
                                .equals(INVESTIGATED_METHOD_NAME))
                .findAny().get();

        final ClassMethod reserveItemMethod = (ClassMethod) reserveItemMethodMapping.getStatementListContainer();
        final BasicComponent basicComponent = reserveItemMethodMapping.getSeff()
                .getBasicComponent_ServiceEffectSpecification();

        final MethodCallFinder methodCallFinder = new MethodCallFinder();
        final EJBFunctionClassificationStrategy ejbFunctionClassificationStrategy = new EJBFunctionClassificationStrategy(
                analysisResult.getSourceCodeDecoratorRepository(), basicComponent, analysisResult.getRoot(),
                methodCallFinder);
        final FunctionCallClassificationVisitor visitor = new FunctionCallClassificationVisitor(
                ejbFunctionClassificationStrategy, methodCallFinder);

        final List<BitSet> bitSets = new ArrayList<BitSet>();
        for (final Statement statement : reserveItemMethod.getStatements()) {
            final Collection<BitSet> currentBitSet = visitor.doSwitch(statement);
            bitSets.addAll(currentBitSet);
        }
        return bitSets;

    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(final List<BitSet> bitSets) {
        this.assertAnnotations(bitSets, FunctionCallType.INTERNAL, FunctionCallType.EXTERNAL, FunctionCallType.LIBRARY,
                FunctionCallType.LIBRARY, FunctionCallType.EXTERNAL,
                FunctionCallType.INTERNAL_CALL_CONTAINING_EXTERNAL_CALL);
    }

    @Override
    protected void assertTestSingleComponent(final List<BitSet> bitSets) {
    	this.assertAnnotations(bitSets, FunctionCallType.INTERNAL, FunctionCallType.LIBRARY, 
    			FunctionCallType.LIBRARY, FunctionCallType.INTERNAL);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final List<BitSet> bitSets) {
        this.assertAnnotations(bitSets, FunctionCallType.INTERNAL, FunctionCallType.LIBRARY, FunctionCallType.LIBRARY,
                FunctionCallType.INTERNAL);
    }
    
    @Override
	protected void assertTestComponentWithProvidedEventInterface(List<BitSet> bitSets) {
		this.assertAnnotations(bitSets, FunctionCallType.INTERNAL, FunctionCallType.LIBRARY, FunctionCallType.INTERNAL);
	}
    
    @Override
	protected void assertTestTwoComponentsWithProvidedEventInterfaceAndRequiredInterface(List<BitSet> bitSets) {
    	this.assertAnnotations(bitSets, FunctionCallType.INTERNAL, FunctionCallType.EMITEVENT,FunctionCallType.LIBRARY, 
    			FunctionCallType.INTERNAL_CALL_CONTAINING_EXTERNAL_CALL);
	}

    private void assertAnnotations(final List<BitSet> bitSets, final FunctionCallType... expectedFunctionCallTypes) {
    	//remove empty bit sets from list
    	List<BitSet> filteredBitSets = bitSets.stream().filter(bitSet->!bitSet.isEmpty()).collect(Collectors.toList());
        Assert.assertEquals("Expected  length of bit sets does not match the actual length",
                expectedFunctionCallTypes.length, filteredBitSets.size());
        for (int i = 0; i < filteredBitSets.size(); i++) {
            final BitSet bitSet = filteredBitSets.get(i);
            final FunctionCallType expectedFunctionCallType = expectedFunctionCallTypes[i];
            final BitSet expectedBitSet = new BitSet();
            expectedBitSet.set(FunctionCallClassificationVisitor.getIndex(expectedFunctionCallType));
            if (expectedFunctionCallType.equals(FunctionCallType.INTERNAL_CALL_CONTAINING_EXTERNAL_CALL)) {
                expectedBitSet.set(FunctionCallClassificationVisitor.getIndex(FunctionCallType.INTERNAL));
            }

            Assert.assertEquals(
                    "The expected bit set does not have the same cardinality as the actual bit set. Position of failure: "
                            + i,
                    expectedBitSet.cardinality(), bitSet.cardinality());
        }
    }

}
