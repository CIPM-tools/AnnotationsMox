package org.somox.ejbmox.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.annotations.AnnotationParameter;
import org.emftext.language.java.annotations.AnnotationParameterList;
import org.emftext.language.java.annotations.AnnotationValue;
import org.emftext.language.java.annotations.SingleAnnotationParameter;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.references.IdentifierReference;
import org.emftext.language.java.types.Type;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.somox.analyzer.AnalysisResult;
import org.somox.kdmhelper.GetAccessedType;
import org.somox.sourcecodedecorator.InterfaceSourceCodeLink;
import org.somox.util.PCMModelCreationHelper;
import org.somox.util.Seff2JavaCreatorUtil;

/**
 * Creates the PCM repository based on EJB components
 *
 * @author langhamm
 *
 */
public class EJBmoxPCMRepositoryModelCreator {

    private static final boolean IS_EXTENSION_FOR_MEDIA_STORE_ENABLED = true;

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

        // TODO
        findTransactionAttributesForEJBMethods(ejbClass);
    }

    private void findTransactionAttributesForEJBMethods(Class ejbClass) {
        List<AnnotationInstance> annotationInstances = EJBAnnotationHelper.getAnnotations(ejbClass,
                "TransactionAttribute");
        if (!annotationInstances.isEmpty()) {
            // select the last annotation instance, if there are more than one
            AnnotationInstance annotationInstance = annotationInstances.get(annotationInstances.size() - 1);

            //
            AnnotationParameter parameter = annotationInstance.getParameter();
            if (parameter instanceof SingleAnnotationParameter) {
                SingleAnnotationParameter singleParameter = (SingleAnnotationParameter) parameter;
                IdentifierReference reference = (IdentifierReference) singleParameter.getValue();
                Enumeration enumeration = (Enumeration) reference.getTarget();

                System.out.println(enumeration.getQualifiedName());
                AnnotationValue v;
            } else if (parameter instanceof AnnotationParameterList) {
                System.out.println("list");
            }
        } else {
            // TODO
        }

        for (AnnotationInstance i : annotationInstances) {
            System.out.println(i.getAnnotation().getName());
        }

    }

    private BasicComponent createBasicComponentForEJBClass(final Class ejbClass) {
        final BasicComponent basicComponent = RepositoryFactory.eINSTANCE.createBasicComponent();
        basicComponent.setEntityName(ejbClass.getName());
        this.repository.getComponents__Repository().add(basicComponent);
        this.sourceCodeDecoratorHelper.createComponentImplementingClassesLink(basicComponent, ejbClass);
        return basicComponent;
    }

    private Collection<OperationInterface> createInterfacesForEJBClass(final Class ejbClass) {
        final List<Interface> directImplementedInterfaces = ejbClass.getImplements().stream()
                .map(typeReference -> GetAccessedType.getAccessedType(typeReference))
                .filter(type -> type instanceof Interface).map(jaMoPPInterface -> (Interface) jaMoPPInterface)
                .filter(jaMoPPif -> this.isEJBRelevantInterface(jaMoPPif)).collect(Collectors.toList());
        logger.info(
                "implementedInterfaces for class " + ejbClass.getName() + ": " + directImplementedInterfaces.size());
        final Collection<OperationInterface> opInterfaces = new ArrayList<OperationInterface>();
        switch (directImplementedInterfaces.size()) {
        case 0:
            if (IS_EXTENSION_FOR_MEDIA_STORE_ENABLED) {
                EJBmoxPCMRepositoryModelCreator.logger.info("No implementing interfaces for EJB class "
                        + ejbClass.getName() + " found. Assume that the public methods of the class are the interface");
                createArchitecturalOpInterfaceForClass(ejbClass, opInterfaces);
            } else {
                EJBmoxPCMRepositoryModelCreator.logger
                        .warn("No implementing interface for EJB class " + ejbClass.getName() + " found.");
            }
            break;
        case 1:
            // the implemented interface is by definition an EJB Buisness
            // Interface
            this.createArchitecturalInterfaceForEJBInterface(directImplementedInterfaces.get(0), opInterfaces, null);
            break;
        default:// >1
            // only those interfaces that are annotated with @Local or @Remote
            // are business
            // interfaces
            directImplementedInterfaces.stream()
                    .filter(implemententedInterface -> EJBAnnotationHelper
                            .isEJBBuisnessInterface(implemententedInterface))
                    .forEach(buisnessInterface -> this.createArchitecturalInterfaceForEJBInterface(buisnessInterface,
                            opInterfaces, null));
            break;
        }
        this.repository.getInterfaces__Repository().addAll(opInterfaces);
        return opInterfaces;

    }

    private void createArchitecturalOpInterfaceForClass(Class ejbClass, Collection<OperationInterface> opInterfaces) {
        Optional<InterfaceSourceCodeLink> checkForAlreadyExistingInteface = checkForAlreadyExistingInteface(ejbClass);
        if (checkForAlreadyExistingInteface.isPresent()) {
            // interface already created --> do nothing
            return;
        }
        final OperationInterface opInterface = createOperationSignatureAndUpdateSCDM(ejbClass, opInterfaces);
        ejbClass.getMethods().stream().filter(method -> method.isPublic())
                .forEach(publicMethod -> this.pcmModelCreationHelper
                        .createOperationSignatureInInterfaceForJaMoPPMemberAndUpdateSourceCodeDecorator(opInterface,
                                this.repository, publicMethod));

    }

    private void createArchitecturalInterfaceForEJBInterface(final Interface jaMoPPInterface,
            final Collection<OperationInterface> opInterfaces, final OperationInterface possibleChildInterface) {
        final Optional<InterfaceSourceCodeLink> existingInterfaceSourceCodeLink = checkForAlreadyExistingInteface(
                jaMoPPInterface);
        if (existingInterfaceSourceCodeLink.isPresent()) {
            // not create a new one, only add the new child as child of the
            final org.palladiosimulator.pcm.repository.Interface pcmInterface = existingInterfaceSourceCodeLink.get()
                    .getInterface();
            if (null != possibleChildInterface) {
                possibleChildInterface.getParentInterfaces__Interface().add(pcmInterface);
            }

            return;
        }
        final OperationInterface opInterface = createOperationSignatureAndUpdateSCDM(jaMoPPInterface, opInterfaces);
        for (final Member jaMoPPMember : jaMoPPInterface.getMembers()) {
            this.pcmModelCreationHelper.createOperationSignatureInInterfaceForJaMoPPMemberAndUpdateSourceCodeDecorator(
                    opInterface, this.repository, jaMoPPMember);
        }
        jaMoPPInterface.getAllSuperClassifiers().stream().filter(type -> type instanceof Interface)
                .map(jaMoPPIf -> (Interface) jaMoPPIf).filter(jaMoPPIf -> this.isEJBRelevantInterface(jaMoPPIf))
                .forEach(relevantInterface -> this.createArchitecturalInterfaceForEJBInterface(relevantInterface,
                        opInterfaces, opInterface));
    }

    private OperationInterface createOperationSignatureAndUpdateSCDM(final ConcreteClassifier concreteClassifier,
            final Collection<OperationInterface> opInterfaces) {
        final OperationInterface opInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
        opInterface.setEntityName(concreteClassifier.getName());
        opInterfaces.add(opInterface);
        this.sourceCodeDecoratorHelper.createInterfaceSourceCodeLink(opInterface, concreteClassifier);
        return opInterface;
    }

    private Optional<InterfaceSourceCodeLink> checkForAlreadyExistingInteface(
            final ConcreteClassifier concreteClassifier) {
        final Optional<InterfaceSourceCodeLink> existingInterfaceSourceCodeLink = this.analysisResult
                .getSourceCodeDecoratorRepository().getInterfaceSourceCodeLink().stream()
                .filter(interfaceSourceCodeLink -> interfaceSourceCodeLink.getGastClass().equals(concreteClassifier))
                .findAny();
        return existingInterfaceSourceCodeLink;
    }

    private void createOperationProvidedRole(final BasicComponent basicComponent,
            final OperationInterface providedOpInterface) {
        final OperationProvidedRole opr = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
        opr.setProvidedInterface__OperationProvidedRole(providedOpInterface);
        opr.setProvidingEntity_ProvidedRole(basicComponent);
        opr.setEntityName(basicComponent.getEntityName() + "_provides_" + providedOpInterface.getEntityName());
    }

    private boolean isEJBRelevantInterface(final Interface implementedInterfaces) {
        return !(implementedInterfaces.getName().equals("Serializable")
                || implementedInterfaces.getName().equals("Externalizable")
                || implementedInterfaces.getContainingPackageName().toString().startsWith("javax.ejb"));
    }

    private void createRequiredRoles(final BasicComponent basicComponent, final Class ejbClass) {
        final List<Field> ejbFields = ejbClass.getMembers().stream().filter(member -> member instanceof Field)
                .map(member -> (Field) member).filter(field -> isEJBRelevantField(field)).collect(Collectors.toList());
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

    /**
     * A fied is EJB relevant if it has the @EJB annotation.If extensions are enabled EJBmox also
     * considers fields that have an corresponding architectural interface as EJB interfaces.
     * 
     * @param field
     *            the field to investigate
     * @return whether the field is architectural relevant (true) or not (false)
     */
    private boolean isEJBRelevantField(Field field) {
        if (EJBAnnotationHelper.hasEJBAnnotation(field)) {
            return true;
        }
        if (IS_EXTENSION_FOR_MEDIA_STORE_ENABLED) {
            final Type accessedType = GetAccessedType.getAccessedType(field.getTypeReference());
            OperationInterface opIf = this.sourceCodeDecoratorHelper
                    .findPCMOperationInterfaceForJaMoPPType(accessedType);
            return null != opIf;
        }
        return false;
    }

    private void createEmptySEFFs() {
        Seff2JavaCreatorUtil.executeSeff2JavaAST(this.analysisResult, this.analysisResult.getRoot());
    }

}
