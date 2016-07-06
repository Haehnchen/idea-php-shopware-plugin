package de.espend.idea.shopware.installer.project.dict;

import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerVersion {

    private String version;
    private String presentableName;

    public ShopwareInstallerVersion(@NotNull String version) {
        this.version = version;
        this.presentableName = version;
    }

    public ShopwareInstallerVersion(@NotNull String version, @NotNull String presentableName) {
        this.version = version;
        this.presentableName = presentableName;
    }

    public String getPresentableName() {
        return presentableName;
    }

    public String getVersion() {
        return version;
    }

}
