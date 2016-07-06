package de.espend.idea.shopware.installer.project;

import de.espend.idea.shopware.installer.project.dict.ShopwareInstallerVersion;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareInstallerSettings {

    @NotNull
    private final ShopwareInstallerVersion version;

    @NotNull
    private final String phpInterpreter;

    public ShopwareInstallerSettings(@NotNull ShopwareInstallerVersion version, @NotNull String phpInterpreter) {
        this.version = version;
        this.phpInterpreter = phpInterpreter;
    }

    public boolean isDownload() {
        return true;
    }

    @NotNull
    public ShopwareInstallerVersion getVersion() {
        return version;
    }

    @NotNull
    public String getPhpInterpreter() {
        return phpInterpreter;
    }

    public String getExistingPath() {
        // @TODO: implement
        return "";
    }

}
