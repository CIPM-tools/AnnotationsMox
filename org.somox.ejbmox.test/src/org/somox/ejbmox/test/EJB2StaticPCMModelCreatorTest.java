package org.somox.ejbmox.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.somox.analyzer.AnalysisResult;
import org.somox.ejbmox.analyzer.EJB2StaticPCMModelCreator;
import org.somox.ejbmox.test.mock.DummyModelAnalyzer;
import org.somox.kdmhelper.KDMReader;
import org.somox.kdmhelper.metamodeladdition.Root;
import org.splevo.jamopp.extraction.JaMoPPSoftwareModelExtractor;

public class EJB2StaticPCMModelCreatorTest {

    private static final String NAME_OF_SINGLE_COMPONENT = "InventoryServiceBean";
    private static final String NAME_OF_SINGLE_INTERFACE = "InventoryService";
    private static final String NAME_OF_PROV_INTERFACE = NAME_OF_SINGLE_INTERFACE;
    private static final String NAME_OF_REQ_INTERFACE = "QueryService";

    private static String TEST_CODE_FOLDER_NAME = "testcode";
    private static String TEST_OUTPUT_FOLDER_NAME = "testmodel";

    private AnalysisResult analysisResult;

    @BeforeClass
    public static void beforeClass() {
        EJBmoxTestUtil.registerMetamodels();
        EJBmoxTestUtil.initializeLogger();
        EJBmoxTestUtil.setupURIPathmaps();
        final File outputFolder = new File(TEST_OUTPUT_FOLDER_NAME + "/");
        final File[] listFiles = outputFolder.listFiles();
        if (null != listFiles) {
            final List<File> files = Arrays.asList(listFiles);
            files.forEach(file -> file.delete());
        }
    }

    @Before
    public void beforeTest() {
        final DummyModelAnalyzer dummyModelAnalyzer = new DummyModelAnalyzer();
        this.analysisResult = dummyModelAnalyzer.initializeAnalysisResult();
    }

    @Test
    public void testSingleComponent() {
        final Repository repository = this.executeTest();

        this.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
    }

    @Test
    public void testComponentWithProvidedInterface() {
        final Repository repository = this.executeTest();

        final BasicComponent bc = this.assertOneBasicComponentWithName(repository, NAME_OF_SINGLE_COMPONENT);
        final OperationInterface opIf = this.assertOneInterfaceWithName(repository, NAME_OF_SINGLE_INTERFACE);
        this.assertProvidedRoleBetween(bc, opIf);
    }

    @Test
    public void testTwoComponentsWithProvidedAndRequiredInterface() {
        final Repository repository = this.executeTest();
        this.assertEntriesInCollection(repository.getComponents__Repository(), 2);
        final BasicComponent bc = (BasicComponent) repository.getComponents__Repository().stream()
                .filter(comp -> comp.getEntityName().equals(NAME_OF_SINGLE_COMPONENT)).findAny().get();
        this.assertEntriesInCollection(repository.getInterfaces__Repository(), 2);
        final OperationInterface providedInterface = this.claimOperationInterfaceWithName(repository,
                NAME_OF_PROV_INTERFACE);
        final OperationInterface requiredInterface = this.claimOperationInterfaceWithName(repository,
                NAME_OF_REQ_INTERFACE);
        this.assertProvidedRoleBetween(bc, providedInterface);
        this.assertRequiredRoleBetween(bc, requiredInterface);
    }

    private Repository executeTest() {
        final String testMethodName = getTestMethodName();
        final String path = TEST_CODE_FOLDER_NAME + "/" + testMethodName;
        final JaMoPPSoftwareModelExtractor jaMoPPSoftwareModelExtractor = new JaMoPPSoftwareModelExtractor();
        jaMoPPSoftwareModelExtractor.extractSoftwareModel(Arrays.asList(path), new NullProgressMonitor());
        final List<Resource> resources = jaMoPPSoftwareModelExtractor.getSourceResources();
        final KDMReader kdmReader = new KDMReader();
        kdmReader.addModelsToRoot(resources);
        final Root root = kdmReader.getRoot();
        this.analysisResult.setRoot(root);
        final EJB2StaticPCMModelCreator ejb = new EJB2StaticPCMModelCreator(root.getCompilationUnits(),
                this.analysisResult);
        ejb.createStaticArchitectureModel();

        this.saveModel(this.analysisResult.getInternalArchitectureModel(), testMethodName, "repository");
        this.saveModel(this.analysisResult.getSourceCodeDecoratorRepository(), testMethodName, "sourcecodedecorator");

        return this.analysisResult.getInternalArchitectureModel();
    }

    private void saveModel(final EObject eObject, final String name, final String fileExtension) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        final URI fileURI = URI.createFileURI(TEST_OUTPUT_FOLDER_NAME + "/" + name + "." + fileExtension);
        final Resource resource = resourceSet.createResource(fileURI);
        resource.getContents().add(eObject);
        try {
            resource.save(null);
        } catch (final IOException e) {
            throw new RuntimeException("Could not save eObject " + name + "." + fileExtension, e);
        }
    }

    private BasicComponent assertOneBasicComponentWithName(final Repository repository, final String expectedName) {
        final BasicComponent basicComponent = (BasicComponent) this
                .assertSingleEntryInCollection(repository.getComponents__Repository());
        this.assertEntityName(expectedName, basicComponent.getEntityName());
        return basicComponent;
    }

    private OperationInterface assertOneInterfaceWithName(final Repository repository, final String expectedName) {
        final OperationInterface opInterface = (OperationInterface) this
                .assertSingleEntryInCollection(repository.getInterfaces__Repository());
        this.assertEntityName(expectedName, opInterface.getEntityName());
        return opInterface;
    }

    private void assertProvidedRoleBetween(final BasicComponent bc, final OperationInterface opIf) {
        final OperationProvidedRole opr = (OperationProvidedRole) this
                .assertSingleEntryInCollection(bc.getProvidedRoles_InterfaceProvidingEntity());
        Assert.assertEquals("Operation provided interface is wrong", opr.getProvidedInterface__OperationProvidedRole(),
                opIf);
    }

    private void assertRequiredRoleBetween(final BasicComponent bc, final OperationInterface requiredInterface) {
        final OperationRequiredRole orr = (OperationRequiredRole) this
                .assertSingleEntryInCollection(bc.getRequiredRoles_InterfaceRequiringEntity());
        Assert.assertEquals("Operation required interface is wrong", orr.getRequiredInterface__OperationRequiredRole(),
                requiredInterface);

    }

    private Object assertSingleEntryInCollection(final Collection<?> collection) {
        return this.assertEntriesInCollection(collection, 1);
    }

    private Object assertEntriesInCollection(final Collection<?> collection, final int entries) {
        Assert.assertEquals("There should be excactly " + entries + " element(s) in the collection " + collection,
                entries, collection.size());
        return collection.iterator().next();
    }

    private void assertEntityName(final String expectedName, final String name) {
        Assert.assertEquals("The name of the created PCM element is wrong", expectedName, name);
    }

    private OperationInterface claimOperationInterfaceWithName(final Repository repository, final String ifName) {
        return (OperationInterface) repository.getInterfaces__Repository().stream()
                .filter(opIf -> opIf.getEntityName().equals(ifName)).findAny().get();
    }

    /**
     * Copied from:
     * http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method
     * Get the method name for the calling method of the getMethodMethod.
     *
     * @return method name
     */
    private static String getTestMethodName() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[3].getMethodName();
    }

}
