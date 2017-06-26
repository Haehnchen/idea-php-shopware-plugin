package de.espend.idea.license.request;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class RefreshRequest {
    @NotNull
    @SerializedName("plugin_id")
    private final String pluginId;

    @NotNull
    private String token;

    public RefreshRequest(@NotNull String pluginId, @NotNull String token) {
        this.pluginId = pluginId;
        this.token = token;
    }

    @NotNull
    public String getToken() {
        return token;
    }

    @NotNull
    public String getPluginId() {
        return pluginId;
    }
}
