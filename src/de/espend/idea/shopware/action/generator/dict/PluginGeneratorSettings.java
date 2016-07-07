package de.espend.idea.shopware.action.generator.dict;

import org.jetbrains.annotations.NotNull;

public class PluginGeneratorSettings {

    @NotNull
    private final String pluginName;

    public PluginGeneratorSettings(@NotNull String pluginName) {

        this.pluginName = pluginName;
    }

    @NotNull
    public String getPluginName() {
        return pluginName;
    }
}
