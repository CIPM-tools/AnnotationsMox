package org.somox.ejbmox.pcmtx.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcmtx.api.EntityTypesAPI;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.pcmtx.model.ParsedSQLStatement;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class CreateEntityTypesJob extends AbstractPCMTXJob {

    private static final String PATH_ENTITY_TYPES_REPOSITORY = "entitytypes.resourcetype";

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        ResourceRepository repository = EntityTypesAPI.createEmptyRepository();

        // calculate set of distinct table names
        PCMTXPartition partition = getPCMTXPartition();
        Set<String> tableNames = new HashSet<>();
        for (ParsedSQLStatement stmt : partition.getParsedStatementsMap().values()) {
            tableNames.addAll(stmt.getTableNames());
        }

        // create entity types from table names, assuming a 1:1 relationship
        for (String tableName : tableNames) {
            String entityName = tableNameToCamelCase(tableName);
            EntityTypesAPI.createEntityType(repository, entityName);
        }

        saveModelToFile(repository);
    }

    public String tableNameToCamelCase(String tableName) {
        String[] segments = tableName.split("_|\\."); // underscore or dot
        for (int i = 0; i < segments.length; i++) {
            String s = segments[i];
            if (s.length() <= 1) {
                continue;
            }
            String first = s.substring(0, 1);
            String remainder = s.substring(1, s.length());
            segments[i] = first.toUpperCase() + remainder.toLowerCase();
        }
        return String.join("", segments);
    }

    public void saveModelToFile(EObject root) {
        EJBmoxConfiguration ejbMoXConfig = getPCMTXPartition().getConfiguration().getEjbMoXConfiguration();
        String projectIdentifier = ejbMoXConfig.getFileLocations().getProjectName();
        String outputFolder = ejbMoXConfig.getFileLocations().getOutputFolder();
        URI uri = createURI(projectIdentifier, outputFolder, PATH_ENTITY_TYPES_REPOSITORY);

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

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Create Entity Types";
    }

}
