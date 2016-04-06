package org.somox.ejbmox.inspectit2pcm.model;

/**
 * Represenation of a method (i.e., method metadata).
 * 
 * @author Patrice Bouillet (parts of this class have been copied from
 *         InspectIT)
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
		return getPackageName() + "." + getClassName() + "." + getMethodName();
	}

}
