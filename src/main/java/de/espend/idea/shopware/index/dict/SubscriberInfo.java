package de.espend.idea.shopware.index.dict;

import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SubscriberInfo {

    @NotNull
    private final BootstrapResource event;

    @NotNull
    private final String service;

    public SubscriberInfo(@NotNull BootstrapResource event, @NotNull String service) {
        this.event = event;
        this.service = service;
    }

    @NotNull
    public BootstrapResource getEvent() {
        return event;
    }

    @NotNull
    public String getService() {
        return service;
    }
}
