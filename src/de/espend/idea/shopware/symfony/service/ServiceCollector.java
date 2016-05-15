package de.espend.idea.shopware.symfony.service;

import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import de.espend.idea.shopware.index.dict.BootstrapResource;
import de.espend.idea.shopware.index.dict.ServiceResource;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import fr.adrienbrault.idea.symfony2plugin.dic.container.SerializableService;
import fr.adrienbrault.idea.symfony2plugin.dic.container.ServiceInterface;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollectorParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceCollector implements fr.adrienbrault.idea.symfony2plugin.extension.ServiceCollector {

    private static final Key<CachedValue<Collection<ServiceInterface>>> SERVICE_CACHE = new Key<>("SW_SERVICE_CACHE");
    private static final Key<CachedValue<Collection<String>>> SERVICE_NAME_CACHE = new Key<>("SW_SERVICE_NAME_CACHE");

    @Override
    public void collectServices(@NotNull ServiceCollectorParameter.Service arg) {

        // cache
        CachedValue<Collection<ServiceInterface>> cache = arg.getProject().getUserData(SERVICE_CACHE);
        if (cache == null) {
            cache = CachedValuesManager.getManager(arg.getProject())
                .createCachedValue(new MyServiceCollectionCachedValueProvider(arg), false);

            arg.getProject().putUserData(SERVICE_CACHE, cache);
        }

        arg.addAll(cache.getValue());
    }

    @Override
    public void collectIds(@NotNull ServiceCollectorParameter.Id arg) {

        // cache
        CachedValue<Collection<String>> cache = arg.getProject().getUserData(SERVICE_NAME_CACHE);
        if (cache == null) {
            cache = CachedValuesManager.getManager(arg.getProject()).createCachedValue(new MyServiceNameCachedValueProvider(arg), false);
            arg.getProject().putUserData(SERVICE_NAME_CACHE, cache);
        }

        arg.addAll(cache.getValue());
    }

    private static class MyServiceCollectionCachedValueProvider implements CachedValueProvider<Collection<ServiceInterface>> {
        private final ServiceCollectorParameter.Service args;

        MyServiceCollectionCachedValueProvider(ServiceCollectorParameter.Service args) {
            this.args = args;
        }

        @Nullable
        @Override
        public Result<Collection<ServiceInterface>> compute() {
            return Result.create(
                ContainerUtil.map(SubscriberIndexUtil.getIndexedBootstrapResources(args.getProject(), BootstrapResource.INIT_RESOURCE), (Function<ServiceResource, ServiceInterface>) resource
                    -> new SerializableService(resource.getServiceName()).setClassName(SubscriberIndexUtil.getTypeForResource(args.getProject(), resource)))
                , PsiModificationTracker.MODIFICATION_COUNT
            );
        }
    }

    private static class MyServiceNameCachedValueProvider implements CachedValueProvider<Collection<String>> {
        private final ServiceCollectorParameter.Id args;

        MyServiceNameCachedValueProvider(ServiceCollectorParameter.Id args) {
            this.args = args;
        }

        @Nullable
        @Override
        public Result<Collection<String>> compute() {
            return Result.create(
                ContainerUtil.map(SubscriberIndexUtil.getIndexedBootstrapResources(args.getProject()), ServiceResource::getServiceName),
                PsiModificationTracker.MODIFICATION_COUNT
            );
        }
    }
}
