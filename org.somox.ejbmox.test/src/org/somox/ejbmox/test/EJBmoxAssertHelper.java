package org.somox.ejbmox.test;

import java.util.Collection;

import org.junit.Assert;

public class EJBmoxAssertHelper {

    public static Object assertSingleEntryInCollection(final Collection<?> collection) {
        return EJBmoxAssertHelper.assertEntriesInCollection(collection, 1);
    }

    public static Object assertEntriesInCollection(final Collection<?> collection, final int entries) {
        Assert.assertEquals("There should be excactly " + entries + " element(s) in the collection " + collection,
                entries, collection.size());
        return collection.iterator().next();
    }

    public static void assertEntityName(final String expectedName, final String entityName) {
        assertEntityName(expectedName, entityName, true);
    }

    public static void assertEntityName(final String expectedName, final String entityName, final boolean exactMatch) {
        if (exactMatch) {
            Assert.assertEquals("The name of the created PCM element is wrong", expectedName, entityName);
        } else {
            Assert.assertTrue(
                    "The enitity name " + entityName + " does not containt the expected name: " + expectedName,
                    entityName.contains(expectedName));
        }

    }

}
