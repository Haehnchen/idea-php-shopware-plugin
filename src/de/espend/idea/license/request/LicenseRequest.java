package de.espend.idea.license.request;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class LicenseRequest {
    @NotNull
    @SerializedName("plugin_id")
    private String pluginId;

    @NotNull
    @SerializedName("plugin_version")
    private String pluginVersion;

    @NotNull
    @SerializedName("code")
    private String licenseCode;

    @NotNull
    private final String username;

    @NotNull
    private final String password;

    public LicenseRequest(@NotNull String pluginId, @NotNull String pluginVersion, @NotNull String licenseCode, @NotNull String username, @NotNull String password) {
        this.pluginId = pluginId;
        this.pluginVersion = pluginVersion;
        this.licenseCode = licenseCode;
        this.username = username;
        this.password = password;
    }

    @NotNull
    public String getPluginId() {
        return pluginId;
    }

    @NotNull
    public String getPluginVersion() {
        return pluginVersion;
    }

    @NotNull
    public String getLicenseCode() {
        return licenseCode;
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @NotNull
    public String getPassword() {
        return password;
    }
}
