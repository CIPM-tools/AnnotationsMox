package org.somox.ejbmox.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.util.EJBmoxUtil;

public abstract class EJBmoxAbstractTest<T> {

    protected static final String NAME_OF_SINGLE_COMPONENT = "InventoryServiceBean";
    protected static final String NAME_OF_SINGLE_INTERFACE = "InventoryService";
    protected static final String NAME_OF_PROV_INTERFACE = NAME_OF_SINGLE_INTERFACE;
    protected static final String NAME_OF_REQ_INTERFACE = "QueryService";
    protected static final String NAME_OF_REQ_COMPONENT = "QueryServiceBean";
    public static final String INVESTIGATED_METHOD_NAME = "reserveItem";

    protected abstract void beforeTest();

    protected abstract T executeTest(String testMethodName);

    protected abstract void assertTestTwoComponentsWithProvidedAndRequiredInterface(T t);

    protected abstract void assertTestSingleComponent(T t);

    protected abstract void assertTestComponentWithProvidedInterface(T t);

    @BeforeClass
    public static void beforeClass() {
        EJBmoxUtil.registerMetamodels();
        EJBmoxUtil.initializeLogger();
        EJBmoxUtil.setupURIPathmaps();
        final File outputFolder = new File(EJBmoxTestUtil.TEST_OUTPUT_FOLDER_NAME + "/");
        final File[] listFiles = outputFolder.listFiles();
        if (null != listFiles) {
            final List<File> files = Arrays.asList(listFiles);
            files.forEach(file -> file.delete());
        }
    }

    @Before
    public void beforeTestCase() {
        this.beforeTest();
    }

    @Test
    public void testSingleComponent() {
        final T t = this.executeTest(getTestMethodName());
        this.assertTestSingleComponent(t);
    }

    @Test
    public void testComponentWithProvidedInterface() {
        final T t = this.executeTest(getTestMethodName());
        this.assertTestComponentWithProvidedInterface(t);
    }

    @Test
    public void testTwoComponentsWithProvidedAndRequiredInterface() {
        final T t = this.executeTest(getTestMethodName());
        this.assertTestTwoComponentsWithProvidedAndRequiredInterface(t);
    }

    /**
     * Copied from:
     * http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method
     * Get the method name for the calling method of the getMethodMethod.
     *
     * @return method name
     */
    protected static String getTestMethodName() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[2].getMethodName();
    }

}
