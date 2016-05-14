package de.espend.idea.shopware.index.dict;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceResources implements Serializable {

    private Collection<ServiceResource> serviceResources = new ArrayList<>();

    public ServiceResources(@NotNull Collection<ServiceResource> serviceResources) {
        this.serviceResources = serviceResources;
    }

    public Collection<ServiceResource> getServiceResources() {
        return serviceResources;
    }
}
