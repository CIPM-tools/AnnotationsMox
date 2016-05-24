package org.somox.ejbmox.util;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.somox.configuration.FileLocationConfiguration;

/**
 * Utility methods for storing EMF model elements into XMI files.
 * 
 * @author Philipp Merkle
 *
 */
public class EMFHelper {

    private EMFHelper() {
        // don't instantiate
    }

    /**
     * Creates a new {@link Resource} for {@code root} and stores it as XMI file to the location
     * derived from the {@code locationConfiguration}.
     * 
     * @param root
     *            the element to be stored
     * @param filename
     *            the filename of the XMI file to be created
     * @param locationConfiguration
     *            the location configuration
     * @param logger
     */
    public static void createResourceAndSave(EObject root, String filename,
            FileLocationConfiguration locationConfiguration, Logger logger) {
        String projectIdentifier = locationConfiguration.getProjectName();
        String outputFolder = locationConfiguration.getOutputFolder();
        URI uri = createURI(projectIdentifier, outputFolder, filename);

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

        final Resource resource = resourceSet.createResource(uri);
        resource.getContents().add(root);

        final HashMap<Object, Object> saveOptions = new HashMap<Object, Object>();
        saveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);

        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * Saves a modified resource.
     * 
     * @param resource
     *            the resource to be saved
     * @param logger
     */
    public static void save(Resource resource, Logger logger) {
        try {
            final HashMap<Object, Object> saveOptions = new HashMap<>();
            saveOptions.put(XMIResource.OPTION_URI_HANDLER, new URIHandlerImpl.PlatformSchemeAware());
            resource.save(saveOptions);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private static URI createURI(String projectIdentifier, String outputFolder, String fileName) {
        String[] segments = new String[] { projectIdentifier, outputFolder, fileName };
        URI uri = URI.createPlatformResourceURI(stripLeadingOrTrailingSlashes(segments[0]), true);
        for (int i = 1; i < segments.length; i++) {
            String normalizedSegment = stripLeadingOrTrailingSlashes(segments[i]);
            uri = uri.appendSegment(normalizedSegment);
        }
        return uri;
    }

    private static String stripLeadingOrTrailingSlashes(String original) {
        return original.replaceAll("^/+", "").replaceAll("/+$", "");
    }

}
