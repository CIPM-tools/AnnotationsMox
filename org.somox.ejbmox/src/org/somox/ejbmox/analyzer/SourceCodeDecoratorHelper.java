package org.somox.ejbmox.analyzer;

import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.types.Type;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.somox.sourcecodedecorator.ComponentImplementingClassesLink;
import org.somox.sourcecodedecorator.InterfaceSourceCodeLink;
import org.somox.sourcecodedecorator.SourceCodeDecoratorRepository;
import org.somox.sourcecodedecorator.SourcecodedecoratorFactory;

public class SourceCodeDecoratorHelper {

    private final SourceCodeDecoratorRepository sourceCodeDecorator;

    public SourceCodeDecoratorHelper(final SourceCodeDecoratorRepository sourceCodeDecoratorRepository) {
        this.sourceCodeDecorator = sourceCodeDecoratorRepository;
    }

    public void createComponentImplementingClassesLink(final BasicComponent basicComponent, final Class jaMoPPClass) {
        final ComponentImplementingClassesLink componentImplementingClassLink = SourcecodedecoratorFactory.eINSTANCE
                .createComponentImplementingClassesLink();
        componentImplementingClassLink.setComponent(basicComponent);
        componentImplementingClassLink.getImplementingClasses().add(jaMoPPClass);
        this.sourceCodeDecorator.getComponentImplementingClassesLink().add(componentImplementingClassLink);
    }

    public void createInterfaceSourceCodeLink(final OperationInterface opInterface, final ConcreteClassifier jaMoPPInterface) {
        final InterfaceSourceCodeLink interfaceSourceCodeLink = SourcecodedecoratorFactory.eINSTANCE
                .createInterfaceSourceCodeLink();
        interfaceSourceCodeLink.setInterface(opInterface);
        interfaceSourceCodeLink.setGastClass(jaMoPPInterface);
        this.sourceCodeDecorator.getInterfaceSourceCodeLink().add(interfaceSourceCodeLink);
    }

    public OperationInterface findPCMOperationInterfaceForJaMoPPType(final Type type) {
        final InterfaceSourceCodeLink opInterfaceSourceCodeLink = this.sourceCodeDecorator.getInterfaceSourceCodeLink()
                .stream()
                .filter(interfaceSourceCodeLink -> null != interfaceSourceCodeLink.getGastClass()
                        && null != interfaceSourceCodeLink.getInterface()
                        && interfaceSourceCodeLink.getGastClass().equals(type)
                        && interfaceSourceCodeLink.getInterface() instanceof OperationInterface)
                .findAny().orElse(null);

        return null == opInterfaceSourceCodeLink ? null : (OperationInterface) opInterfaceSourceCodeLink.getInterface();
    }

}
