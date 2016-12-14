package org.somox.ejbmox.analyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.types.Type;
import org.somox.kdmhelper.KDMHelper;

public final class EJBAnnotationHelper {

	private static final Logger logger = Logger.getLogger(EJBAnnotationHelper.class.getSimpleName());

	private static final String STATELESS_ANNOTATION_NAME = "Stateless";
	private static final String STATEFUL_ANNOTATION_NAME = "Stateful";
	private static final String MESSAGE_DRIVEN_ANNOTATION_NAME = "MessageDriven";
	private static final String LOCAL_ANNOTATION_NAME = "Local";
	private static final String REMOTE_ANNOTATION_NAME = "Remote";
	private static final String EJB_ANNOTATION_NAME = "EJB";
	private static final String DEPENDENT_ANNOTATION_NAME = "Dependent";
	private static final String INJECT_ANNOTATION_NAME = "Inject";
	private static final String JAVAX_ENTERPRISE_EVENT_CLASS_FQN = "javax.enterprise.event.Event";
	private static final String OBSERVES_ANNOTATION_NAME = "Observes";

	static final Set<String> EJB_COMPONENT_ANNOTATION_NAMES = new HashSet<String>(
			Arrays.asList(EJBAnnotationHelper.STATELESS_ANNOTATION_NAME, EJBAnnotationHelper.STATEFUL_ANNOTATION_NAME,
					EJBAnnotationHelper.MESSAGE_DRIVEN_ANNOTATION_NAME, EJBAnnotationHelper.DEPENDENT_ANNOTATION_NAME));
	static final Set<String> EJB_BUISNESS_INTERFACE_ANNOTATION_NAMES = new HashSet<String>(
			Arrays.asList(EJBAnnotationHelper.LOCAL_ANNOTATION_NAME, EJBAnnotationHelper.REMOTE_ANNOTATION_NAME));

	private EJBAnnotationHelper() {
	}

	public static boolean isEJBClass(final Class jamoppClass) {
		final boolean found = EJBAnnotationHelper.hasAnnoations(jamoppClass,
				EJBAnnotationHelper.EJB_COMPONENT_ANNOTATION_NAMES);
		if (found) {
			logger.info("Found EJB class: " + jamoppClass.getQualifiedName());
		}
		return found;
	}

	public static boolean filterAnnotationName(final AnnotationInstance annotation,
			final Set<String> annotationClassifiersToCheck) {
		final Classifier annotationClassifier = annotation.getAnnotation();
		if (null != annotationClassifier) {
			return annotationClassifiersToCheck.contains(annotationClassifier.getName());
		}
		return false;
	}

	public static boolean hasAnnoations(final AnnotableAndModifiable annotableAndModifiable,
			final Set<String> annotationClassifiersToCheck) {
		return annotableAndModifiable.getAnnotationsAndModifiers().stream()
				.filter(annotationOrModifier -> annotationOrModifier instanceof AnnotationInstance)
				.map(annotation -> (AnnotationInstance) annotation).filter(annotation -> EJBAnnotationHelper
						.filterAnnotationName(annotation, annotationClassifiersToCheck))
				.collect(Collectors.toList()).size() > 0;
	}

	public static List<AnnotationInstance> getAnnotations(final AnnotableAndModifiable annotableAndModifiable,
			final String annotationClassifierToCheck) {
		Set<String> classifierSet = new HashSet<>();
		classifierSet.add(annotationClassifierToCheck);
		return getAnnotations(annotableAndModifiable, classifierSet);
	}

	public static List<AnnotationInstance> getAnnotations(final AnnotableAndModifiable annotableAndModifiable,
			final Set<String> annotationClassifiersToCheck) {
		return annotableAndModifiable.getAnnotationsAndModifiers().stream()
				.filter(annotationOrModifier -> annotationOrModifier instanceof AnnotationInstance)
				.map(annotation -> (AnnotationInstance) annotation).filter(annotation -> EJBAnnotationHelper
						.filterAnnotationName(annotation, annotationClassifiersToCheck))
				.collect(Collectors.toList());
	}

	public static boolean isEJBBuisnessInterface(final Interface implemententedInterface) {
		final boolean found = EJBAnnotationHelper.hasAnnoations(implemententedInterface,
				EJBAnnotationHelper.EJB_BUISNESS_INTERFACE_ANNOTATION_NAMES);
		if (found) {
			logger.info("Found EJB buisness interface " + implemententedInterface.getQualifiedName());
		}
		return found;
	}

	public static boolean hasEJBAnnotation(final Field field) {
		final boolean found = EJBAnnotationHelper.hasAnnoations(field, new HashSet<String>(
				Arrays.asList(EJBAnnotationHelper.EJB_ANNOTATION_NAME, EJBAnnotationHelper.INJECT_ANNOTATION_NAME)));
		if (found) {
			logger.info("Found field with EJB annotation: " + field.getName() + " in class: "
					+ field.getContainingConcreteClassifier().getQualifiedName());
		}
		return found;
	}

	public static boolean isEJBEventType(final Type accessedType) {
		if (accessedType instanceof Classifier) {
			final Classifier accessedClassifier = (Classifier) accessedType;
			if (KDMHelper.computeFullQualifiedName(accessedClassifier).equals(JAVAX_ENTERPRISE_EVENT_CLASS_FQN)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasEventParameter(Method method) {
		boolean hasEventParam = method.getParameters().stream()
				.filter(param -> isEventParameter(param))
				.findAny().isPresent();
		return hasEventParam;
	}

	public static boolean isEventParameter(Parameter param) {
		return EJBAnnotationHelper.hasAnnoations(param,
				new HashSet<String>(Arrays.asList(EJBAnnotationHelper.OBSERVES_ANNOTATION_NAME)));
	}

}
