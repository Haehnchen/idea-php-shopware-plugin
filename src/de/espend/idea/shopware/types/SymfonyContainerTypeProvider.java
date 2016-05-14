package de.espend.idea.shopware.types;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import fr.adrienbrault.idea.symfony2plugin.Settings;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpTypeProviderUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Adrien Brault <adrien.brault@gmail.com>
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SymfonyContainerTypeProvider implements PhpTypeProvider2 {

    final static char TRIM_KEY = '\u0200';

    @Override
    public char getKey() {
        return '\u0199';
    }

    @Nullable
    @Override
    public String getType(PsiElement e) {

        if (DumbService.getInstance(e.getProject()).isDumb() || !Settings.getInstance(e.getProject()).pluginEnabled || !Settings.getInstance(e.getProject()).symfonyContainerTypeProvider) {
            return null;
        }

        // container calls are only on "get" methods
        if(!(e instanceof MethodReference) || !PhpElementsUtil.isMethodWithFirstStringOrFieldReference(e, "get")) {
            return null;
        }

        return PhpTypeProviderUtil.getReferenceSignature((MethodReference) e, TRIM_KEY);
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, final Project project) {

        // get back our original call
        // since phpstorm 7.1.2 we need to validate this
        int endIndex = expression.lastIndexOf(TRIM_KEY);
        if(endIndex == -1) {
            return Collections.emptySet();
        }

        String originalSignature = expression.substring(0, endIndex);
        String parameter = expression.substring(endIndex + 1);

        // search for called method
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<? extends PhpNamedElement> phpNamedElementCollections = phpIndex.getBySignature(originalSignature, null, 0);
        if(phpNamedElementCollections.size() == 0) {
            return Collections.emptySet();
        }

        // get first matched item
        PhpNamedElement phpNamedElement = phpNamedElementCollections.iterator().next();
        if(!(phpNamedElement instanceof Method)) {
            return phpNamedElementCollections;
        }

        parameter = PhpTypeProviderUtil.getResolvedParameter(phpIndex, parameter);
        if(parameter == null) {
            return phpNamedElementCollections;
        }

        // finally search the classes
        if(new Symfony2InterfacesUtil().isContainerGetCall((Method) phpNamedElement) && !PhpElementsUtil.isInstanceOf(((Method) phpNamedElement).getContainingClass(), "Shopware\\Components\\DependencyInjection\\Container")) {
            return phpNamedElementCollections;
        }

        Collection<ServiceResource> values = SubscriberIndexUtil.getIndexedBootstrapResources(project, BootstrapResource.INIT_RESOURCE);

        final Collection<PhpClass> classes = new HashSet<>();
        for(ServiceResource value : values) {
            String signature = value.getSignature();
            if(signature == null) {
                continue;
            }

            String[] split = signature.split("\\.");
            Method classMethod = PhpElementsUtil.getClassMethod(project, split[0], split[1]);
            if(classMethod == null) {
                continue;
            }

            classMethod.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {

                    if (element instanceof PhpReturn) {
                        PhpReturn returnElement = (PhpReturn) element;
                        PhpPsiElement firstPsiChild = returnElement.getFirstPsiChild();
                        if (firstPsiChild instanceof PhpTypedElement) {
                            PhpType type = ((PhpTypedElement) firstPsiChild).getType();
                            classes.addAll(PhpElementsUtil.getClassFromPhpTypeSet(project, type.getTypes()));
                        }
                    }

                    super.visitElement(element);
                }

            });
        }

        return classes;
    }

}
