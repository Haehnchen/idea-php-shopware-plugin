package de.espend.idea.shopware.index.utils;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtils;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.dict.ServiceResources;
import de.espend.idea.shopware.index.dict.SubscriberInfo;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import fr.adrienbrault.idea.symfony2plugin.stubs.SymfonyProcessors;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SubscriberIndexUtil {

    private static final String[] ENLIGHT_BOOTSTRAP_RESOURCE = new String[] {
        "Enlight_Bootstrap_InitResource_",
        "Enlight_Bootstrap_AfterInitResource_",
        "Enlight_Bootstrap_AfterRegisterResource_",
    };

    /**
     * foo => 'goo'
     * foo => ['goo', ... ]
     */
    @Nullable
    public static String getMethodNameForEventValue(@NotNull PhpPsiElement value) {
        if(value instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) value).getContents();
        }

        if(value instanceof ArrayCreationExpression) {
            PhpPsiElement firstPsiChild = value.getFirstPsiChild();
            if(firstPsiChild != null && firstPsiChild.getNode().getElementType() == PhpElementTypes.ARRAY_VALUE) {
                StringLiteralExpression stringLiteral = ObjectUtils.tryCast(firstPsiChild.getFirstPsiChild(), StringLiteralExpression.class);
                if(stringLiteral != null) {
                    return stringLiteral.getContents();
                }
            }

            return null;
        }

        return null;
    }

    @Nullable
    public static SubscriberInfo getSubscriberInfo(@NotNull String name) {
        for (String event : ENLIGHT_BOOTSTRAP_RESOURCE) {
            if(!name.startsWith(event)) {
                continue;
            }

            BootstrapResource resource = BootstrapResource.fromString(StringUtils.stripEnd(event, "_"));
            if(resource == null) {
                return null;
            }

            return new SubscriberInfo(
                resource,
                name.substring(event.length())
            );
        }

        return null;
    }

    @NotNull
    public static Collection<ServiceResource> getIndexedBootstrapResources(@NotNull Project project) {
        return getIndexedBootstrapResources(project, BootstrapResource.INIT_RESOURCE, BootstrapResource.AFTER_INIT_RESOURCE, BootstrapResource.AFTER_REGISTER_RESOURCE);
    }

    @NotNull
    public static Collection<ServiceResource> getIndexedBootstrapResources(@NotNull Project project, @NotNull BootstrapResource... bootstrapResources) {
        Collection<ServiceResource> resources = new ArrayList<>();

        for (BootstrapResource bootstrapResource : bootstrapResources) {
            FileBasedIndexImpl.getInstance()
                .getValues(InitResourceServiceIndex.KEY, bootstrapResource.getText(), GlobalSearchScope.allScope(project))
                .forEach(resource -> resources.addAll(resource.getServiceResources()));
        }

        return resources;
    }
}
