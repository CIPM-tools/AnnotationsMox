package org.somox.ejbmox.inspectit2pcm.model;

/**
 * Represenation of a method (i.e., method metadata).
 * 
 * @author Patrice Bouillet (parts of this class have been copied from InspectIT)
 * @author Philipp Merkle
 *
 */
public class MethodIdent {

    private Long id;

    private String packageName;

    private String className;

    private String methodName;

    private String returnType;

    private int modifiers;

    public Long getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getReturnType() {
        return returnType;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String toFQN() {
        return toFQN(false);
    }

    public String toFQN(boolean excludeMethodName) {
        if (excludeMethodName) {
            return getPackageName() + "." + getClassName();
        } else {
            return getPackageName() + "." + getClassName() + "." + getMethodName();
        }
    }

    public boolean isWrapperFor(MethodIdent other) {
        return false;
    }
    
    public boolean isWrapper() {
        return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodIdent other = (MethodIdent) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
