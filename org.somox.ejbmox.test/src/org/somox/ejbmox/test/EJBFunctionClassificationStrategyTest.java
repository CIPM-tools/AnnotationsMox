package org.somox.ejbmox.test;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.statements.Statement;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.ejb.functionclassification.EJBFunctionClassificationStrategy;
import org.somox.ejbmox.test.mock.DummyModelAnalyzer;
import org.somox.gast2seff.visitors.FunctionCallClassificationVisitor;
import org.somox.gast2seff.visitors.FunctionCallClassificationVisitor.FunctionCallType;
import org.somox.gast2seff.visitors.MethodCallFinder;
import org.somox.sourcecodedecorator.SEFF2MethodMapping;
import org.somox.test.gast2seff.visitors.JaMoPP2PCMBaseTest;

public class EJBFunctionClassificationStrategyTest extends EJBmoxAbstractTest<Map<Commentable, List<BitSet>>> {

    private static final String INVESTIGATED_METHOD_NAME = "reserveItem";

    @Override
    protected void beforeTest() {
    }

    @Override
    protected Map<Commentable, List<BitSet>> executeTest(final String testMethodName) {
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

        for (final Statement statement : reserveItemMethod.getStatements()) {
            visitor.doSwitch(statement);
        }
        return visitor.getAnnotations();

    }

    @Override
    protected void assertTestTwoComponentsWithProvidedAndRequiredInterface(
            final Map<Commentable, List<BitSet>> aonntations) {
        this.assertAnnotations(aonntations, FunctionCallType.INTERNAL, FunctionCallType.EXTERNAL,
                FunctionCallType.LIBRARY, FunctionCallType.LIBRARY, FunctionCallType.EXTERNAL,
                FunctionCallType.INTERNAL_CALL_CONTAINING_EXTERNAL_CALL, FunctionCallType.EXTERNAL);
    }

    @Override
    protected void assertTestSingleComponent(final Map<Commentable, List<BitSet>> aonntations) {
        this.assertAnnotations(aonntations, FunctionCallType.INTERNAL, FunctionCallType.LIBRARY,
                FunctionCallType.LIBRARY, FunctionCallType.INTERNAL);
    }

    @Override
    protected void assertTestComponentWithProvidedInterface(final Map<Commentable, List<BitSet>> aonntations) {
        this.assertAnnotations(aonntations, FunctionCallType.INTERNAL, FunctionCallType.LIBRARY,
                FunctionCallType.LIBRARY, FunctionCallType.INTERNAL);
    }

    private void assertAnnotations(final Map<Commentable, List<BitSet>> aonntations,
            final FunctionCallType... expectedFunctionCallTypes) {
        JaMoPP2PCMBaseTest.assertBitSetsForType(aonntations, Commentable.class, expectedFunctionCallTypes);

    }

}
