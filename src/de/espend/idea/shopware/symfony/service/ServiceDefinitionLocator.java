package de.espend.idea.shopware.symfony.service;

import com.jetbrains.php.lang.psi.elements.Method;
import de.espend.idea.shopware.index.utils.SubscriberIndexUtil;
import fr.adrienbrault.idea.symfony2plugin.extension.ServiceDefinitionLocatorParameter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceDefinitionLocator implements fr.adrienbrault.idea.symfony2plugin.extension.ServiceDefinitionLocator {

    @Override
    public void locate(@NotNull String service, @NotNull ServiceDefinitionLocatorParameter parameter) {
        SubscriberIndexUtil.getIndexedBootstrapResources(parameter.getProject()).forEach(resource -> {
            if(!service.equalsIgnoreCase(resource.getServiceName())) {
                return;
            }

            Method method = SubscriberIndexUtil.getMethodForResource(parameter.getProject(), resource);
            if(method != null) {
                parameter.addTarget(method);
            }
        });
    }
}
