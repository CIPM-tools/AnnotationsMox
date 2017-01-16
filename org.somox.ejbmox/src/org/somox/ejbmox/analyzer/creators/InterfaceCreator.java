package org.somox.ejbmox.analyzer.creators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.types.Type;
import org.palladiosimulator.pcm.repository.EventGroup;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.Repository;
import org.somox.ejbmox.analyzer.EJBAnnotationHelper;
import org.somox.ejbmox.analyzer.EJBmoxPCMRepositoryModelCreator;
import org.somox.kdmhelper.GetAccessedType;
import org.somox.sourcecodedecorator.InterfaceSourceCodeLink;
import org.somox.sourcecodedecorator.SourceCodeDecoratorRepository;
import org.somox.util.PCMModelCreationHelper;
import org.somox.util.SourceCodeDecoratorHelper;

public class InterfaceCreator {

	private static final Logger logger = Logger.getLogger(InterfaceCreator.class.getSimpleName());

	private final Repository repository;
	private final SourceCodeDecoratorRepository sourceCodeDecorator;
	public final SourceCodeDecoratorHelper sourceCodeDecoratorHelper;
	private final PCMModelCreationHelper pcmModelCreationHelper;

	public InterfaceCreator(Repository repository, SourceCodeDecoratorRepository sourceCodeDecorator,
			SourceCodeDecoratorHelper sourceCodeDecoratorHelper, PCMModelCreationHelper pcmModelCreationHelper) {
		this.repository = repository;
		this.sourceCodeDecorator = sourceCodeDecorator;
		this.sourceCodeDecoratorHelper = sourceCodeDecoratorHelper;
		this.pcmModelCreationHelper = pcmModelCreationHelper;

	}

	public Collection<org.palladiosimulator.pcm.repository.Interface> createProvidedInterfacesForEJBClass(
			final Class ejbClass) {
		final List<Interface> directImplementedInterfaces = ejbClass.getImplements().stream()
				.map(typeReference -> GetAccessedType.getAccessedType(typeReference))
				.filter(type -> type instanceof Interface).map(jaMoPPInterface -> (Interface) jaMoPPInterface)
				.filter(jaMoPPif -> this.isEJBRelevantInterface(jaMoPPif)).collect(Collectors.toList());
		logger.info(
				"implementedInterfaces for class " + ejbClass.getName() + ": " + directImplementedInterfaces.size());
		final Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces = new ArrayList<>();
		switch (directImplementedInterfaces.size()) {
		case 0:
			if (EJBmoxPCMRepositoryModelCreator.EXTENSIONS_FOR_FIELDS_AND_INTERFACES) {
				logger.info("No implementing interfaces for EJB class " + ejbClass.getName()
						+ " found. Assume that the public methods of the class are the interface");
				createArchitecturalOpInterfaceForClass(ejbClass, pcmInterfaces);
			} else {
				logger.warn("No implementing interface for EJB class " + ejbClass.getName() + " found.");
			}
			break;
		case 1:
			// the implemented interface is by definition an EJB Buisness
			// Interface
			this.createArchitecturalInterfaceForEJBInterface(directImplementedInterfaces.get(0), pcmInterfaces, null);
			break;
		default:// >1
			// only those interfaces that are annotated with @Local or @Remote
			// are business
			// interfaces
			directImplementedInterfaces.stream().filter(
					implemententedInterface -> EJBAnnotationHelper.isEJBBuisnessInterface(implemententedInterface))
					.forEach(buisnessInterface -> this.createArchitecturalInterfaceForEJBInterface(buisnessInterface,
							pcmInterfaces, null));
			break;
		}
		this.repository.getInterfaces__Repository().addAll(pcmInterfaces);
		return pcmInterfaces;

	}

	private void createArchitecturalOpInterfaceForClass(Class ejbClass,
			Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces) {
		Optional<InterfaceSourceCodeLink> checkForAlreadyExistingInteface = checkForAlreadyExistingInteface(ejbClass);
		if (checkForAlreadyExistingInteface.isPresent()) {
			// interface already created --> do nothing
			return;
		}
		final OperationInterface opInterface = pcmModelCreationHelper.createOperationInterfaceAndUpdateSCDM(ejbClass);
		pcmInterfaces.add(opInterface);
		ejbClass.getMethods().stream().filter(method -> method.isPublic())
				.forEach(publicMethod -> this.pcmModelCreationHelper
						.createOperationSignatureInInterfaceForJaMoPPMemberAndUpdateSourceCodeDecorator(opInterface,
								this.repository, publicMethod));

	}

	private void createArchitecturalInterfaceForEJBInterface(final Interface jaMoPPInterface,
			final Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces,
			final org.palladiosimulator.pcm.repository.Interface possibleChildInterface) {
		// decide whether to create an OpInterface or an EventGroup or both
		List<Method> opInterfaceMethods = new ArrayList<Method>();
		List<Method> eventGroupMethods = new ArrayList<Method>();
		for (Method method : jaMoPPInterface.getMethods()) {
			if (EJBAnnotationHelper.hasEventParameter(method)) {
				eventGroupMethods.add(method);
			} else {
				opInterfaceMethods.add(method);
			}
		}
		logger.info("Found " + eventGroupMethods.size() + " event methods and " + opInterfaceMethods.size()
				+ " operation interface methods in " + jaMoPPInterface.getName());

		createOpIfAndSignatures(opInterfaceMethods, jaMoPPInterface, pcmInterfaces, possibleChildInterface);
		createEventGroupsAndEventTypes(eventGroupMethods, jaMoPPInterface, pcmInterfaces);

	}

	private Optional<InterfaceSourceCodeLink> checkForAlreadyExistingInteface(
			final ConcreteClassifier concreteClassifier) {
		final Optional<InterfaceSourceCodeLink> existingInterfaceSourceCodeLink = this.sourceCodeDecorator
				.getInterfaceSourceCodeLink().stream()
				.filter(interfaceSourceCodeLink -> interfaceSourceCodeLink.getGastClass().equals(concreteClassifier))
				.findAny();
		return existingInterfaceSourceCodeLink;
	}

	private void createOpIfAndSignatures(List<Method> opInterfaceMethods, final Interface jaMoPPInterface,
			final Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces,
			org.palladiosimulator.pcm.repository.Interface possibleChildInterface) {
		if (opInterfaceMethods.isEmpty()) {
			return;
		}
		org.palladiosimulator.pcm.repository.Interface pcmInterface = null;
		final Optional<InterfaceSourceCodeLink> existingInterfaceSourceCodeLink = checkForAlreadyExistingInteface(
				jaMoPPInterface);
		if (existingInterfaceSourceCodeLink.isPresent()) {
			// not create a new one, only add the new child
			pcmInterface = existingInterfaceSourceCodeLink.get().getInterface();
			if (null != possibleChildInterface) {
				possibleChildInterface.getParentInterfaces__Interface().add(pcmInterface);
			}

		} else {
			pcmInterface = this.pcmModelCreationHelper.createOperationInterfaceAndUpdateSCDM(jaMoPPInterface);
			for (final Member jaMoPPMember : opInterfaceMethods) {
				this.pcmModelCreationHelper
						.createOperationSignatureInInterfaceForJaMoPPMemberAndUpdateSourceCodeDecorator(
								(OperationInterface) pcmInterface, this.repository, jaMoPPMember);
			}
		}
		pcmInterfaces.add(pcmInterface);
		createSuperInterfaces(jaMoPPInterface, pcmInterfaces, pcmInterface);
	}

	private void createEventGroupsAndEventTypes(List<Method> eventGroupMethods, Interface jaMoPPInterface,
			Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces) {
		if (eventGroupMethods.isEmpty()) {
			return;
		}
		for (Method method : eventGroupMethods) {
			Parameter observedJaMoPPParameter = findRelevantJaMoPPParameter(method);
			ConcreteClassifier observedEventDataType = getObservedEventDataType(observedJaMoPPParameter);
			EventGroup eventGroup = this.sourceCodeDecoratorHelper
					.findPCMInterfaceForJaMoPPType(observedEventDataType, EventGroup.class);
			if (null == eventGroup) {
				eventGroup = this.pcmModelCreationHelper.createEventGroupAndEventTypeAndUpdateSourceCodeDecorator(
						observedEventDataType, repository, observedJaMoPPParameter, method);
			}
			pcmInterfaces.add(eventGroup);
		}
		createSuperInterfaces(jaMoPPInterface, pcmInterfaces, null);
	}

	private ConcreteClassifier getObservedEventDataType(Parameter relevantJaMoPPParameter) {
		Type targetType = relevantJaMoPPParameter.getTypeReference().getTarget();
		if (null == targetType || !(targetType instanceof ConcreteClassifier)) {
			throw new RuntimeException("Parameter has wrong target type: " + relevantJaMoPPParameter);
		}
		return (ConcreteClassifier) targetType;
	}

	private Parameter findRelevantJaMoPPParameter(Method method) {
		Parameter relevantJaMoPPParameter = method.getParameters().stream()
				.filter(param -> EJBAnnotationHelper.isEventParameter(param)).findFirst().get();
		return relevantJaMoPPParameter;
	}

	private void createSuperInterfaces(final Interface jaMoPPInterface,
			final Collection<org.palladiosimulator.pcm.repository.Interface> pcmInterfaces,
			final org.palladiosimulator.pcm.repository.Interface pcmInterface) {
		jaMoPPInterface.getAllSuperClassifiers().stream().filter(type -> type instanceof Interface)
				.map(jaMoPPIf -> (Interface) jaMoPPIf).filter(jaMoPPIf -> this.isEJBRelevantInterface(jaMoPPIf))
				.forEach(relevantInterface -> this.createArchitecturalInterfaceForEJBInterface(relevantInterface,
						pcmInterfaces, pcmInterface));
	}

	private boolean isEJBRelevantInterface(final Interface implementedInterfaces) {
		return !(implementedInterfaces.getName().equals("Serializable")
				|| implementedInterfaces.getName().equals("Externalizable")
				|| implementedInterfaces.getContainingPackageName().toString().startsWith("javax.ejb"));
	}

}