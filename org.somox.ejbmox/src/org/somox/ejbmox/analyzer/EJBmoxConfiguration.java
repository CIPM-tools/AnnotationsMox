package org.somox.ejbmox.analyzer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.configuration.SoMoXConfiguration;

public class EJBmoxConfiguration extends AbstractMoxConfiguration {

    public static final String EJBMOX_INSPECTIT_FILE_PATHS = "inspectItFilePaths";

    private final Set<String> inspectITFilePaths = new HashSet<String>();

    public EJBmoxConfiguration(final Map<String, Object> attributeMap) {
        this.applyAttributeMap(attributeMap);
    }

    public EJBmoxConfiguration() {
        this.getFileLocations().setOutputFolder("model/");
    }

    public Set<String> getInspectITFilePaths() {
        return this.inspectITFilePaths;
    }

    public void setInspectITFilePaths(final String... filePaths) {
        this.inspectITFilePaths.addAll(Arrays.asList(filePaths));
    }

    /**
     * needed for some methods that need to deal with a {@link SoMoXConfiguration} and can not deal
     * with AbstractMoxConfguration easily.
     */
    public SoMoXConfiguration convertToSoMoXConfiguration() {
        final SoMoXConfiguration somoxConfiguration = new SoMoXConfiguration(this.toMap());
        return somoxConfiguration;

    }

    @Override
    public void applyAttributeMap(final Map<String, Object> attributeMap) {
        if (null == attributeMap) {
            return;
        }
        super.applyAttributeMap(attributeMap);
        if (attributeMap.get(EJBmoxConfiguration.EJBMOX_INSPECTIT_FILE_PATHS) != null) {
            final Object pathsObject = attributeMap.get(AbstractMoxConfiguration.SOMOX_ANALYZER_INPUT_FILE);
            if (pathsObject instanceof Collection<?>) {
                for (final Object pathObject : (Collection<?>) pathsObject) {
                    this.inspectITFilePaths.add((String) pathObject);
                }
            }
        }
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> result = super.toMap();

        result.put(EJBmoxConfiguration.EJBMOX_INSPECTIT_FILE_PATHS, this.inspectITFilePaths);
        return result;

    }
}
