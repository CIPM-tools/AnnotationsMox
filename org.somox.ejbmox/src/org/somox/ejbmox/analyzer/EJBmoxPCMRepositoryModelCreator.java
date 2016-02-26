package org.somox.ejbmox.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.types.Type;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.somox.analyzer.AnalysisResult;
import org.somox.kdmhelper.GetAccessedType;
import org.somox.util.PCMModelCreationHelper;
import org.somox.util.Seff2JavaCreatorUtil;

/**
 * Creates the PCM repository based on EJB components
 *
 * @author langhamm
 *
 */
public class EJBmoxPCMRepositoryModelCreator {

    private static final Logger logger = Logger.getLogger(EJBmoxPCMRepositoryModelCreator.class.getSimpleName());

    private final HashSet<CompilationUnit> compilationUnits;

    private final Repository repository;
    private final PCMModelCreationHelper pcmModelCreationHelper;
    private final SourceCodeDecoratorHelper sourceCodeDecoratorHelper;
    private final AnalysisResult analysisResult;

    /**
     * This map stores the mapping of basic components to its realizing classes internally. It is
     * used in createRequiredRoles to create the required roles for the component.
     */
    private final Map<BasicComponent, Class> basicComponent2EJBClassMap;

    public EJBmoxPCMRepositoryModelCreator(final Collection<CompilationUnit> compilationUnits,
            final AnalysisResult analysisResult) {
        this(new HashSet<CompilationUnit>(compilationUnits), analysisResult);
    }

    public EJBmoxPCMRepositoryModelCreator(final HashSet<CompilationUnit> compilationUnits,
            final AnalysisResult analysisResult) {
        this.compilationUnits = compilationUnits;
        this.analysisResult = analysisResult;
        this.repository = analysisResult.getInternalArchitectureModel();
        this.basicComponent2EJBClassMap = new HashMap<BasicComponent, Class>();
        this.sourceCodeDecoratorHelper = new SourceCodeDecoratorHelper(
                analysisResult.getSourceCodeDecoratorRepository());
        this.pcmModelCreationHelper = new PCMModelCreationHelper(analysisResult);
    }

    public Repository createStaticArchitectureModel() {
        this.compilationUnits.forEach(compilationUnit -> compilationUnit.getClassifiers().stream()
                .filter(classifier -> classifier instanceof Class).map(classifier -> (Class) classifier)
                .filter(jamoppClass -> EJBAnnotationHelper.isEJBClass(jamoppClass))
                .forEach(ejbClass -> this.createArchitectureForEJBClass(ejbClass)));
        this.basicComponent2EJBClassMap.keySet().forEach(
                component -> this.createRequiredRoles(component, this.basicComponent2EJBClassMap.get(component)));
        this.createEmptySEFFs();
        return this.repository;
    }

    private void createArchitectureForEJBClass(final Class ejbClass) {
        final BasicComponent basicComponent = this.createBasicComponentForEJBClass(ejbClass);
        final Collection<OperationInterface> providedOpInterfaces = this.createInterfacesForEJBClass(ejbClass);
        providedOpInterfaces
                .forEach(providedOpInterface -> this.createOperationProvidedRole(basicComponent, providedOpInterface));
        this.basicComponent2EJBClassMap.put(basicComponent, ejbClass);
    }

    private BasicComponent createBasicComponentForEJBClass(final Class ejbClass) {
        final BasicComponent basicComponent = RepositoryFactory.eINSTANCE.createBasicComponent();
        basicComponent.setEntityName(ejbClass.getName());
        this.repository.getComponents__Repository().add(basicComponent);
        this.sourceCodeDecoratorHelper.createComponentImplementingClassesLink(basicComponent, ejbClass);
        return basicComponent;
    }

    private Collection<OperationInterface> createInterfacesForEJBClass(final Class ejbClass) {
        final List<Interface> implementedInterfaces = ejbClass.getImplements().stream()
                .map(typeReference -> GetAccessedType.getAccessedType(typeReference))
                .filter(type -> type instanceof Interface).map(jaMoPPInterface -> (Interface) jaMoPPInterface)
                .collect(Collectors.toList());
        // TODO: filter the Interfaces EJB ignores (e.g. Serializable)
        final Collection<OperationInterface> opInterfaces = new ArrayList<OperationInterface>();
        switch (implementedInterfaces.size()) {
        case 0:
            EJBmoxPCMRepositoryModelCreator.logger
                    .warn("No implementing interface for EJB class " + ejbClass.getName() + " found.");
            break;
        case 1:
            // the implemented interface is by definition an EJB Buisness Interface
            this.createArchitecturalInterfaceForEJBInterface(implementedInterfaces.get(0), opInterfaces);
            break;
        default:// >1
            // only those interfaces that are annotated with @Local or @Remote are business
            // interfaces
            implementedInterfaces.stream()
                    .filter(implemententedInterface -> EJBAnnotationHelper.isEJBBuisnessInterface(implemententedInterface))
                    .forEach(buisnessInterface -> this.createArchitecturalInterfaceForEJBInterface(buisnessInterface,
                            opInterfaces));
            break;
        }
        this.repository.getInterfaces__Repository().addAll(opInterfaces);
        return opInterfaces;

    }

    private void createArchitecturalInterfaceForEJBInterface(final Interface jaMoPPInterface,
            final Collection<OperationInterface> opInterfaces) {
        final OperationInterface opInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
        opInterface.setEntityName(jaMoPPInterface.getName());
        opInterfaces.add(opInterface);
        this.sourceCodeDecoratorHelper.createInterfaceSourceCodeLink(opInterface, jaMoPPInterface);
        for (final Member jaMoPPMember : jaMoPPInterface.getMembers()) {
            this.pcmModelCreationHelper.createOperationSignatureInInterfaceForJaMoPPMemberAndUpdateSourceCodeDecorator(
                    opInterface, this.repository, jaMoPPMember);
        }
    }

    private void createOperationProvidedRole(final BasicComponent basicComponent,
            final OperationInterface providedOpInterface) {
        final OperationProvidedRole opr = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
        opr.setProvidedInterface__OperationProvidedRole(providedOpInterface);
        opr.setProvidingEntity_ProvidedRole(basicComponent);
        opr.setEntityName(basicComponent.getEntityName() + "_provides_" + providedOpInterface.getEntityName());
    }

    private void createRequiredRoles(final BasicComponent basicComponent, final Class ejbClass) {
        final List<Field> ejbFields = ejbClass.getMembers().stream().filter(member -> member instanceof Field)
                .map(member -> (Field) member).filter(field -> EJBAnnotationHelper.hasEJBAnnotation(field))
                .collect(Collectors.toList());
        for (final Field ejbField : ejbFields) {
            final Type accessedType = GetAccessedType.getAccessedType(ejbField.getTypeReference());
            final OperationInterface requiredInterface = this.sourceCodeDecoratorHelper
                    .findPCMOperationInterfaceForJaMoPPType(accessedType);
            if (null == requiredInterface) {
                EJBmoxPCMRepositoryModelCreator.logger.warn("Could not find an OperationInterface for the EJB type: "
                        + accessedType + ". Maybe the interface " + accessedType
                        + " is not provided by a component within the source code");
                continue;
            }
            final OperationRequiredRole orr = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
            orr.setRequiredInterface__OperationRequiredRole(requiredInterface);
            orr.setRequiringEntity_RequiredRole(basicComponent);
            orr.setEntityName(basicComponent.getEntityName() + "_requires_" + requiredInterface.getEntityName());
        }

    }

    private void createEmptySEFFs() {
        Seff2JavaCreatorUtil.executeSeff2JavaAST(this.analysisResult, this.analysisResult.getRoot());
    }

}
