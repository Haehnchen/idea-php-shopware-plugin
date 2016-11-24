package de.espend.idea.shopware.index.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ObjectUtils;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.shopware.index.InitResourceServiceIndex;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.dict.SubscriberInfo;
import fr.adrienbrault.idea.symfony2plugin.stubs.ContainerCollectionResolver;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SubscriberIndexUtil {

    private static final String[] ENLIGHT_BOOTSTRAP_RESOURCE = new String[] {
        "Enlight_Bootstrap_InitResource_",
        "Enlight_Bootstrap_AfterInitResource_",
        "Enlight_Bootstrap_AfterRegisterResource_",
    };

    private static final Key<CachedValue<Collection<ServiceResource>>> SERVICE_RESOURCE = new Key<>("SW_SERVICE_RESOURCE_CACHE");

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

            // dont allow: eg "Enlight_Bootstrap_InitResource_"
            String serviceName = name.substring(event.length());
            if(StringUtils.isBlank(serviceName)) {
                return null;
            }

            return new SubscriberInfo(
                resource,
                serviceName
            );
        }

        return null;
    }

    @NotNull
    public static Collection<ServiceResource> getIndexedBootstrapResources(@NotNull Project project) {

        // cache
        CachedValue<Collection<ServiceResource>> cache = project.getUserData(SERVICE_RESOURCE);
        if (cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> CachedValueProvider.Result.create(
                getIndexedBootstrapResources(project, BootstrapResource.INIT_RESOURCE, BootstrapResource.AFTER_INIT_RESOURCE, BootstrapResource.AFTER_REGISTER_RESOURCE),
                PsiModificationTracker.MODIFICATION_COUNT
            ), false);

            project.putUserData(SERVICE_RESOURCE, cache);
        }

        return cache.getValue();
    }

    @NotNull
    public static Collection<ServiceResource> getIndexedBootstrapResources(@NotNull Project project, @NotNull BootstrapResource... bootstrapResources) {
        Collection<ServiceResource> serviceResources = new ArrayList<>();

        for (BootstrapResource bootstrapResource : bootstrapResources) {
            for (Set<String> resources : FileBasedIndexImpl.getInstance().getValues(InitResourceServiceIndex.KEY, bootstrapResource.getText(), GlobalSearchScope.allScope(project))) {
                if(resources == null) {
                    continue;
                }

                for (String s : resources) {
                    String[] split = s.split(String.valueOf(InitResourceServiceIndex.TRIM_KEY));
                    if(split.length < 4) {
                        continue;
                    }

                    ServiceResource e = new ServiceResource(split[0], split[1], split[2]);
                    e.setSignature(split[3]);

                    serviceResources.add(e);
                }
            }
        }

        return serviceResources;
    }

    @Nullable
    public static Method getMethodForResource(@NotNull Project project, @NotNull ServiceResource resource) {
        String signature = resource.getSignature();
        if(signature == null) {
            return null;
        }

        String[] split = signature.split("\\.");
        if(split.length < 2) {
            return null;
        }

        return PhpElementsUtil.getClassMethod(project, split[0], split[1]);
    }

    @NotNull
    private static Collection<PhpClass> getPhpClassTypeForResource(@NotNull Project project, @NotNull ServiceResource resource) {
        Method method = getMethodForResource(project, resource);
        if(method == null) {
            return Collections.emptyList();
        }

        return PhpElementsUtil.getClassFromPhpTypeSet(project, method.getType().getTypes());
    }

    @Nullable
    public static String getTypeForResource(@NotNull Project project, @NotNull ServiceResource resource) {
        Collection<PhpClass> phpClasses = getPhpClassTypeForResource(project, resource);
        if(phpClasses.size() == 0) {
            return null;
        }

        return StringUtils.stripStart(phpClasses.iterator().next().getFQN(), "\\");
    }

    public static boolean isContainerServiceEvent(@NotNull String event) {
        for (String s : ENLIGHT_BOOTSTRAP_RESOURCE) {
            if(event.startsWith(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isContainerServiceEventAndContains(@NotNull Project project, @NotNull String event) {
        for (String s : ENLIGHT_BOOTSTRAP_RESOURCE) {
            if(event.startsWith(s)) {
                return ContainerCollectionResolver.hasServiceNames(project, event.substring(s.length()));
            }
        }

        return false;
    }
}
