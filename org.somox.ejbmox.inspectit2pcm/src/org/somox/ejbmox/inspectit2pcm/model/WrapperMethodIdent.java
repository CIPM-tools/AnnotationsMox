package org.somox.ejbmox.inspectit2pcm.model;

public class WrapperMethodIdent extends MethodIdent {

    private MethodIdent wrapper;
    private MethodIdent wrapped;

    public WrapperMethodIdent(MethodIdent wrapper, MethodIdent wrapped) {
        this.wrapper = wrapper;
        this.wrapped = wrapped;
    }

    public Long getId() {
        return wrapped.getId();
    }

    public String getMethodName() {
        return wrapped.getMethodName();
    }

    public String getPackageName() {
        return wrapped.getPackageName();
    }

    public String getClassName() {
        return wrapped.getClassName();
    }

    public String getReturnType() {
        return wrapped.getReturnType();
    }

    public int getModifiers() {
        return wrapped.getModifiers();
    }

    public String toFQN() {
        return wrapped.toFQN();
    }

    public String toFQN(boolean excludeMethodName) {
        return wrapped.toFQN(excludeMethodName);
    }

    public int hashCode() {
        return wrapped.hashCode();
    }

    public boolean equals(Object obj) {
        return wrapped.equals(obj);
    }

    public String toString() {
        return wrapped.toString();
    }

    @Override
    public boolean isWrapper() {
        return true;
    }

}
