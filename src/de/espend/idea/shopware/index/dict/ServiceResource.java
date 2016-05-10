package de.espend.idea.shopware.index.dict;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ServiceResource implements Serializable {

    @NotNull
    private String event;

    @NotNull
    private final String serviceName;

    @Nullable
    private String signature;

    @Nullable
    private String type;

    public ServiceResource(@NotNull String event, @NotNull String serviceName) {
        this.event = event;
        this.serviceName = serviceName;
    }

    @NotNull
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

    @NotNull
    public String getServiceName() {
        return serviceName;
    }
}
