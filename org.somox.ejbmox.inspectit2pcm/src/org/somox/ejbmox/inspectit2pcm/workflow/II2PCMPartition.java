package org.somox.ejbmox.inspectit2pcm.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.members.ClassMethod;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.ParametrizationTrace;
import org.somox.sourcecodedecorator.InterfaceSourceCodeLink;
import org.somox.sourcecodedecorator.SEFF2MethodMapping;
import org.somox.sourcecodedecorator.SourceCodeDecoratorRepository;

public class II2PCMPartition {

    private static final Logger logger = Logger.getLogger(II2PCMPartition.class);

    public static final String PARTITION_ID = "org.somox.ejbmox.inspectit2pcm.partition";

    private Map<ResourceDemandingSEFF, String> seffToFQNMap;

    private Map<Interface, String> ifaceToFQNMap;

    private II2PCMConfiguration configuration;

    private PCMParametrization parametrization;

    private ParametrizationTrace trace;

    private II2PCMPartition(Map<ResourceDemandingSEFF, String> seffToFQNMap, Map<Interface, String> ifaceToFQNMap,
            II2PCMConfiguration configuration) {
        this.trace = new ParametrizationTrace();
        this.seffToFQNMap = seffToFQNMap;
        this.ifaceToFQNMap = ifaceToFQNMap;
        this.configuration = configuration;
    }

    public static II2PCMPartition createFrom(SoMoXBlackboard blackboard, II2PCMConfiguration configuration) {
        SourceCodeDecoratorRepository sourceDecorator = blackboard.getAnalysisResult()
                .getSourceCodeDecoratorRepository();

        // build list of external services, together with their fully qualified
        // name (FQN)
        Map<ResourceDemandingSEFF, String> seffToFQNMap = new HashMap<>();
        for (SEFF2MethodMapping m : sourceDecorator.getSeff2MethodMappings()) {
            String fqn = fullyQualifiedName((ClassMethod) m.getStatementListContainer());
            seffToFQNMap.put((ResourceDemandingSEFF) m.getSeff(), fqn);
            logger.debug("Adding SEFF with FQN " + fqn);
        }

        Map<Interface, String> ifaceToFQNMap = new HashMap<>();
        for (InterfaceSourceCodeLink link : sourceDecorator.getInterfaceSourceCodeLink()) {
            String fqn = fullyQualifiedName(link.getGastClass());
            ifaceToFQNMap.put(link.getInterface(), fqn);
            logger.debug("Adding Interface with FQN " + fqn);
        }

        return new II2PCMPartition(seffToFQNMap, ifaceToFQNMap, configuration);
    }

    public Map<ResourceDemandingSEFF, String> getSeffToFQNMap() {
        return seffToFQNMap;
    }

    public Map<Interface, String> getInterfaceToFQNMap() {
        return ifaceToFQNMap;
    }

    public II2PCMConfiguration getConfiguration() {
        return configuration;
    }

    private static String fullyQualifiedName(ConcreteClassifier classifier) {
        return classifier.getQualifiedName();
    }

    private static String fullyQualifiedName(ClassMethod method) {
        return method.getContainingCompilationUnit().getContainedClass().getQualifiedName() + "." + method.getName();
    }

    public PCMParametrization getParametrization() {
        return parametrization;
    }

    public void setParametrization(PCMParametrization parametrization) {
        this.parametrization = parametrization;
    }

    public ParametrizationTrace getTrace() {
        return trace;
    }

}
