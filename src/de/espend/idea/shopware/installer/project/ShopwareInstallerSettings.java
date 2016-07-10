package de.espend.idea.shopware.installer.project;

import de.espend.idea.shopware.installer.project.dict.ShopwareInstallerVersion;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerSettings {

    @NotNull
    private final ShopwareInstallerVersion version;

    public ShopwareInstallerSettings(@NotNull ShopwareInstallerVersion version) {
        this.version = version;
    }

    @NotNull
    public ShopwareInstallerVersion getVersion() {
        return version;
    }

    public String getExistingPath() {
        // @TODO: implement
        return "";
    }
}
