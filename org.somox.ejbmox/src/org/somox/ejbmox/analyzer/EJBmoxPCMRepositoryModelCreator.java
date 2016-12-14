package org.somox.ejbmox.analyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.containers.CompilationUnit;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.analyzer.creators.BasicComponentCreator;
import org.somox.ejbmox.analyzer.creators.InterfaceCreator;
import org.somox.ejbmox.analyzer.creators.ProvidedRoleCreator;
import org.somox.ejbmox.analyzer.creators.RequiredRoleCreator;
import org.somox.util.PCMModelCreationHelper;
import org.somox.util.Seff2JavaCreatorUtil;
import org.somox.util.SourceCodeDecoratorHelper;

/**
 * Creates the PCM repository based on EJB components
 *
 * @author langhamm
 *
 */
public class EJBmoxPCMRepositoryModelCreator {

	public static final boolean EXTENSIONS_FOR_FIELDS_AND_INTERFACES = true;

	private final HashSet<CompilationUnit> compilationUnits;

	private final Repository repository;
	private final PCMModelCreationHelper pcmModelCreationHelper;
	private final SourceCodeDecoratorHelper sourceCodeDecoratorHelper;
	private final AnalysisResult analysisResult;

	private final BasicComponentCreator basicComponentCreator;
	private final InterfaceCreator interfaceCreator;
	private final ProvidedRoleCreator providedRoleCreator;
	private final RequiredRoleCreator requiredRoleCreator;

	public EJBmoxPCMRepositoryModelCreator(final Collection<CompilationUnit> compilationUnits,
			final AnalysisResult analysisResult) {
		this(new HashSet<CompilationUnit>(compilationUnits), analysisResult);
	}

	public EJBmoxPCMRepositoryModelCreator(final HashSet<CompilationUnit> compilationUnits,
			final AnalysisResult analysisResult) {
		this.compilationUnits = compilationUnits;
		this.analysisResult = analysisResult;
		this.repository = analysisResult.getInternalArchitectureModel();
		this.sourceCodeDecoratorHelper = new SourceCodeDecoratorHelper(
				analysisResult.getSourceCodeDecoratorRepository());
		this.pcmModelCreationHelper = new PCMModelCreationHelper(analysisResult, this.sourceCodeDecoratorHelper);

		this.basicComponentCreator = new BasicComponentCreator(this.repository, this.sourceCodeDecoratorHelper);
		this.interfaceCreator = new InterfaceCreator(this.repository,
				this.analysisResult.getSourceCodeDecoratorRepository(), this.sourceCodeDecoratorHelper,
				this.pcmModelCreationHelper);
		this.requiredRoleCreator = new RequiredRoleCreator(this.sourceCodeDecoratorHelper);
		this.providedRoleCreator = new ProvidedRoleCreator();
	}

	public Repository createStaticArchitectureModel() {
		this.compilationUnits.forEach(compilationUnit -> compilationUnit.getClassifiers().stream()
				.filter(classifier -> classifier instanceof Class).map(classifier -> (Class) classifier)
				.filter(jamoppClass -> EJBAnnotationHelper.isEJBClass(jamoppClass))
				.forEach(ejbClass -> this.createArchitectureForEJBClass(ejbClass)));
		Map<BasicComponent, Class> basicComponent2EJBClassMap = this.basicComponentCreator
				.getBasicComponent2EJBClassMap();
		basicComponent2EJBClassMap.keySet()
				.forEach(component -> this.requiredRoleCreator.createRequiredRoles(component, basicComponent2EJBClassMap.get(component)));
		this.createEmptySEFFs();
		return this.repository;
	}

	private void createArchitectureForEJBClass(final Class ejbClass) {
		final BasicComponent basicComponent = this.basicComponentCreator.createBasicComponentForEJBClass(ejbClass);
		final Collection<org.palladiosimulator.pcm.repository.Interface> providedInterfaces = this.interfaceCreator
				.createProvidedInterfacesForEJBClass(ejbClass);
		providedRoleCreator.createProvidedRoles(basicComponent, providedInterfaces);
	}


	private void createEmptySEFFs() {
		Seff2JavaCreatorUtil.executeSeff2JavaAST(this.analysisResult, this.analysisResult.getRoot());
	}

}
