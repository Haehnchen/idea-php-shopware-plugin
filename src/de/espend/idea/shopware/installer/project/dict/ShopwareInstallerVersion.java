package de.espend.idea.shopware.installer.project.dict;

import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerVersion {

    private String version;
    private String presentableName;
    private String url;

    public ShopwareInstallerVersion(@NotNull String version) {
        this.version = version;
        this.presentableName = version;
    }

    public ShopwareInstallerVersion(@NotNull String version, @NotNull String presentableName, @NotNull String url) {
        this.version = version;
        this.presentableName = presentableName;
        this.url = url;
    }

    public String getPresentableName() {
        return presentableName;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }
}
