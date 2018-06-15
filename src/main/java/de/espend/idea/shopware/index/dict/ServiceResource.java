package de.espend.idea.shopware.index.dict;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceResource implements Serializable {

    @Nullable
    private String event;

    @Nullable
    private String subscriber;

    @Nullable
    private String serviceName;

    @Nullable
    private String signature;

    @Nullable
    private String type;

    public ServiceResource() {

    }

    public ServiceResource(@NotNull String event, @NotNull String subscriber, @NotNull String serviceName) {
        this.event = event;
        this.subscriber = subscriber;
        this.serviceName = serviceName;
    }

    @Nullable
    public String getEvent() {
        return event;
    }

    @Nullable
    public String getSignature() {
        return signature;
    }

    public ServiceResource setSignature(@Nullable String signature) {
        this.signature = signature;

        return this;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public ServiceResource setType(@Nullable String type) {
        this.type = type;

        return this;
    }

    @Nullable
    public String getServiceName() {
        return serviceName;
    }

    @Nullable
    public BootstrapResource getSubscriber() {
        return BootstrapResource.fromString(subscriber);
    }
}
