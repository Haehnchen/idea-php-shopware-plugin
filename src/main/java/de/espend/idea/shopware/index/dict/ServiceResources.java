package de.espend.idea.shopware.index.dict;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceResources implements Serializable {

    //private List<ServiceResource> serviceResources = new ArrayList<>();

    public ServiceResources() {
        //this.serviceResources = serviceResources;
    }

    public ServiceResources(@NotNull List<ServiceResource> serviceResources) {
        //this.serviceResources = serviceResources;
    }

    public Collection<ServiceResource> getServiceResources() {
        return Collections.emptyList();
    }
}
