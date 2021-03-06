package org.annotationsmox.test;

import java.io.IOException;

import org.annotationsmox.analyzer.AnnotationsMoxPCMRepositoryModelCreator;
import org.annotationsmox.util.AnnotationsMoxUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.somox.analyzer.AnalysisResult;
import org.somox.kdmhelper.KDMReader;
import org.somox.kdmhelper.metamodeladdition.Root;

public class AnnotationsMoxTestUtil {
	public static String TEST_CODE_FOLDER_NAME = "testcode";
    public static String TEST_OUTPUT_FOLDER_NAME = "testmodel";

    /**
     * disable constructor
     */
    private AnnotationsMoxTestUtil() {
    }

    public static void saveModel(final EObject eObject, final String name, final String fileExtension) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        final URI fileURI = URI
                .createFileURI(AnnotationsMoxTestUtil.TEST_OUTPUT_FOLDER_NAME + "/" + name + "." + fileExtension);
        final Resource resource = resourceSet.createResource(fileURI);
        resource.getContents().add(eObject);
        try {
            resource.save(null);
        } catch (final IOException e) {
            throw new RuntimeException("Could not save eObject " + name + "." + fileExtension, e);
        }
    }

    public static void saveRepoSourceCodeDecoratorAndSystem(final AnalysisResult analysisResult,
            final String testMethodName) {
        saveReposiotryAndSourceCodeDecorator(analysisResult, testMethodName);
        saveModel(analysisResult.getSystemModel(), testMethodName, AnnotationsMoxUtil.SYSTEM_FILE_ENDING);

    }

    public static void saveReposiotryAndSourceCodeDecorator(final AnalysisResult analysisResult,
            final String testMethodName) {
        saveModel(analysisResult.getInternalArchitectureModel(), testMethodName, AnnotationsMoxUtil.REPOSITORY_FILE_ENDING);
        saveModel(analysisResult.getSourceCodeDecoratorRepository(), testMethodName, AnnotationsMoxUtil.SOURCECODEDECORATOR_FILE_ENDING);
    }

    public static void executeEJBmoxPCMRepositoryModelCreator(final String testMethodName,
            final AnalysisResult analysisResult) {
        final String path = TEST_CODE_FOLDER_NAME + "/" + testMethodName;
        final KDMReader kdmReader = new KDMReader();
        try {
            kdmReader.loadProject(path);
        } catch (final IOException e) {
            throw new RuntimeException("Could not load path: " + path, e);
        }
        final Root root = kdmReader.getRoot();
        analysisResult.setRoot(root);
        final AnnotationsMoxPCMRepositoryModelCreator ejb = new AnnotationsMoxPCMRepositoryModelCreator(root.getCompilationUnits(),
                analysisResult);
        ejb.createStaticArchitectureModel();
    }
}
